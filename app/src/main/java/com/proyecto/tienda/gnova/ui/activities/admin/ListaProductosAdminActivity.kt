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
import androidx.appcompat.widget.SearchView

class ListaProductosAdminActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var btnVolver: Button
    private lateinit var adapter: ProductoCrudAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_productos_admin)

        recycler = findViewById(R.id.recyclerProductosAdmin)
        btnVolver = findViewById(R.id.btnVolverDashboard)

        recycler.layoutManager = LinearLayoutManager(this)

        // Inicializar el adaptador con una lista vacía
        adapter = ProductoCrudAdapter(
            productos = emptyList(),
            onEditar = { producto ->
                val intent = Intent(this, EditarProductoActivity::class.java)
                intent.putExtra("producto", producto)
                startActivity(intent)
            },
            onEliminar = { productoId ->
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

        // Configurar SearchView
        val searchView = findViewById<SearchView>(R.id.searchViewProducto)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    adapter.filtrarProductos(it) // Filtrar productos a medida que se escribe
                }
                return true
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ProductoRepositorio.productos.collect { productosActualizados ->
                    adapter.actualizarProductos(productosActualizados)
                }
            }
        }

        btnVolver.setOnClickListener { finish() }
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
