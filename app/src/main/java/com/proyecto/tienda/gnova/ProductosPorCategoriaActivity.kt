package com.proyecto.tienda.gnova

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore

class ProductosPorCategoriaActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var tituloCategoria: TextView
    private lateinit var adapter: ProductoAdapter // Declarar el adaptador como propiedad de la clase
    private lateinit var categoriaSeleccionada: String // Para almacenar la categoría

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos_por_categoria)

        recycler = findViewById(R.id.recyclerProductosCategoria)
        tituloCategoria = findViewById(R.id.tvTituloCategoria)

        categoriaSeleccionada = intent.getStringExtra("categoria") ?: getString(R.string.categoria_general)
        tituloCategoria.text = categoriaSeleccionada // Usar la categoría obtenida o un valor por defecto

        recycler.layoutManager = LinearLayoutManager(this)

        // Inicializar el adaptador con una lista vacía por ahora
        adapter = ProductoAdapter(
            productos = emptyList(), // Empezar con lista vacía
            onItemClick = { producto ->
                val intent = Intent(this, DetalleProductoActivity::class.java)
                intent.putExtra("producto", producto)
                startActivity(intent)
            },
            onAgregarClick = { producto ->
                // Aquí la lógica de agregar al carrito (ya manejada por DetalleProductoActivity y ProductoAdapter)
                // En la vista de productos por categoría, se asume que el botón "Agregar" hace una adición.
                if (producto.stock > 0) {
                    Carrito.agregarProducto(producto)
                    Toast.makeText(this, getString(R.string.msg_agregado_carrito, producto.nombre), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.msg_sin_stock, producto.nombre), Toast.LENGTH_SHORT).show()
                }
            }
        )
        recycler.adapter = adapter

        // Observar cambios en los productos desde el repositorio (Firestore)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ProductoRepositorio.productos.collect { todosLosProductos ->
                    val productosFiltrados = todosLosProductos.filter {
                        it.categoria == categoriaSeleccionada
                    }
                    adapter.actualizarProductos(productosFiltrados) // Actualizar el adaptador con la lista filtrada
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Asegúrate de que la escucha de Firestore esté activa cuando la actividad esté visible
        ProductoRepositorio.iniciarEscuchaProductos()
    }

    override fun onPause() {
        super.onPause()
        // Detén la escucha de Firestore cuando la actividad no esté visible para ahorrar recursos
        ProductoRepositorio.detenerEscuchaProductos()
    }
}
