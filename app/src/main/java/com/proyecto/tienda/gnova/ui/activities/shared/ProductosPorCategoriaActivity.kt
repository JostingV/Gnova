package com.proyecto.tienda.gnova.ui.activities.shared

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.data.models.Producto
import com.proyecto.tienda.gnova.data.models.Carrito
import com.proyecto.tienda.gnova.data.repositories.ProductoRepositorio
import com.proyecto.tienda.gnova.ui.adapters.ProductoGridAdapter
import com.proyecto.tienda.gnova.ui.activities.shared.DetalleProductoActivity

class ProductosPorCategoriaActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var tituloCategoria: TextView
    private lateinit var ivVolver: ImageView
    private lateinit var adapter: ProductoGridAdapter
    private lateinit var categoriaSeleccionada: String
    private lateinit var generoSeleccionado: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos_por_categoria)

        recycler = findViewById(R.id.recyclerProductosCategoria)
        tituloCategoria = findViewById(R.id.tvTituloCategoria)
        ivVolver = findViewById(R.id.ivVolver)

        categoriaSeleccionada = intent.getStringExtra("categoria") ?: "Categoría"
        generoSeleccionado = intent.getStringExtra("genero") ?: "Hombre"
        
        tituloCategoria.text = "$categoriaSeleccionada - $generoSeleccionado"

        // Configurar botón volver
        ivVolver.setOnClickListener {
            finish()
        }

        // Configurar RecyclerView con GridLayoutManager (2 columnas)
        val gridLayoutManager = GridLayoutManager(this, 2)
        recycler.layoutManager = gridLayoutManager

        // Inicializar el adaptador
        adapter = ProductoGridAdapter(
            productos = emptyList(),
            onItemClick = { producto ->
                val intent = Intent(this, DetalleProductoActivity::class.java)
                intent.putExtra("producto", producto)
                startActivity(intent)
            },
            onAgregarClick = { producto ->
                if (producto.stock > 0) {
                    Carrito.agregarProducto(producto)
                    Toast.makeText(this, "Producto ${producto.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Sin stock para ${producto.nombre}", Toast.LENGTH_SHORT).show()
                }
            }
        )
        recycler.adapter = adapter

        // Observar cambios en los productos desde el repositorio
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ProductoRepositorio.productos.collect { todosLosProductos ->
                    val productosFiltrados = todosLosProductos.filter { producto ->
                        producto.categoria.equals(categoriaSeleccionada, ignoreCase = true) &&
                        producto.genero.equals(generoSeleccionado, ignoreCase = true)
                    }
                    adapter.actualizarProductos(productosFiltrados)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ProductoRepositorio.iniciarEscuchaProductos()
    }

    override fun onPause() {
        super.onPause()
        ProductoRepositorio.detenerEscuchaProductos()
    }
}
