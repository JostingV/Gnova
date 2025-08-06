package com.proyecto.tienda.gnova.ui.activities.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.ui.adapters.ProductoCrudAdapter
import com.proyecto.tienda.gnova.data.repositories.ProductoRepositorio

class ListaProductosAdminActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var btnVolver: Button
    private lateinit var adapter: ProductoCrudAdapter // Declarar el adaptador como propiedad de la clase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_productos_admin)

        recycler = findViewById(R.id.recyclerProductosAdmin)
        btnVolver = findViewById(R.id.btnVolverDashboard)

        recycler.layoutManager = LinearLayoutManager(this)

        // Inicializar el adaptador con una lista vacía por ahora
        // El adaptador será actualizado más tarde con los datos de Firestore.
        adapter = ProductoCrudAdapter(
            productos = emptyList(),
            onEditar = { producto ->
                val intent = Intent(this, EditarProductoActivity::class.java)
                intent.putExtra("producto", producto)
                startActivity(intent)
            },
            onEliminar = { productoId -> // El callback ahora recibe el ID del producto (String)
                // En una app real, usarías un AlertDialog o un BottomSheet para confirmar la eliminación.
                Toast.makeText(this, "Eliminando producto...", Toast.LENGTH_SHORT).show()
                ProductoRepositorio.eliminarProducto(
                    productoId,
                    onSuccess = {
                        Toast.makeText(this, "Producto eliminado con éxito", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(this, "Error al eliminar producto: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
        recycler.adapter = adapter

        // Observar cambios en los productos desde el repositorio (Firestore)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ProductoRepositorio.productos.collect { productosActualizados ->
                    // Cuando los productos cambian en Firestore, actualizar el adaptador
                    // Usamos un setter para la lista en el adaptador y llamamos a notifyDataSetChanged.
                    adapter.actualizarProductos(productosActualizados) // Llamar a un método de actualización en el adaptador
                }
            }
        }

        btnVolver.setOnClickListener { finish() }
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
