package com.proyecto.tienda.gnova.ui.activities.client

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.ui.adapters.ProductoAdapter
import com.proyecto.tienda.gnova.data.repositories.ProductoRepositorio
import com.proyecto.tienda.gnova.ui.activities.shared.DetalleProductoActivity

class FavoritosActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ProductoAdapter // Declarar el adaptador como propiedad de la clase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        recycler = findViewById(R.id.recyclerFavoritos)
        recycler.layoutManager = LinearLayoutManager(this)

        // Inicializar el adaptador con la lista de favoritos actual
        // La lista se actualizará en onResume() para asegurar que siempre esté al día.
        adapter = ProductoAdapter(
            productos = ProductoRepositorio.obtenerFavoritos(), // Obtener la lista inicial
            onItemClick = { producto ->
                val intent = Intent(this, DetalleProductoActivity::class.java)
                intent.putExtra("producto", producto) // Producto debe implementar Parcelable
                startActivity(intent)
            },
            onAgregarClick = { producto ->
                // En la vista de favoritos, el botón "Agregar" podría tener un propósito diferente
                // o no ser visible. Aquí puedes decidir si quieres que agregue al carrito,
                // o incluso, si es un botón de favoritos, que lo elimine de favoritos.
                // Para simplificar, asumimos que no tiene un rol activo o lo remueve.
                // ProductoRepositorio.eliminarDeFavoritos(producto) // Ejemplo: eliminar al hacer clic en "Agregar"
                // adapter.actualizarProductos(ProductoRepositorio.obtenerFavoritos()) // Actualizar la lista
            }
        )
        recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Cuando la actividad de Favoritos se reanuda, actualiza la lista
        // para reflejar cualquier cambio (ej. si se agregó/eliminó un favorito desde DetalleProductoActivity)
        val favoritosActualizados = ProductoRepositorio.obtenerFavoritos()
        adapter.actualizarProductos(favoritosActualizados)
    }
}
