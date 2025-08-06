package com.proyecto.tienda.gnova.ui.activities.client

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.data.models.Producto
import com.proyecto.tienda.gnova.data.models.Carrito
import com.proyecto.tienda.gnova.ui.adapters.ProductoAdapter
import com.proyecto.tienda.gnova.ui.activities.shared.DetalleProductoActivity

class CarritoActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var btnFinalizar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        recycler = findViewById(R.id.recyclerCarrito)
        btnFinalizar = findViewById(R.id.btnFinalizarCompra)

        // Obtén tus productos del carrito desde el singleton Carrito
        val productosCarrito: List<Producto> = Carrito.obtenerProductos()

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = ProductoAdapter(
            productos = productosCarrito,
            onItemClick = { producto ->
                val intent = Intent(this, DetalleProductoActivity::class.java)
                intent.putExtra("producto", producto)
                startActivity(intent)
            },
            onAgregarClick = { producto ->
                // En la vista del carrito, el botón "Agregar" podría tener otro propósito,
                // como aumentar la cantidad o simplemente no hacer nada.
                // Por ahora, simplemente muestra un mensaje.
                Toast.makeText(this, "${producto.nombre} ya está en el carrito", Toast.LENGTH_SHORT).show()
            }
        )

        btnFinalizar.setOnClickListener {
            Carrito.limpiarCarrito() // Limpia el carrito después de la compra
            Toast.makeText(this, getString(R.string.msg_compra_realizada), Toast.LENGTH_LONG).show()
            // Opcional: Navegar a una pantalla de confirmación o al inicio
            // startActivity(Intent(this, InicioClienteActivity::class.java))
            finish()
        }
    }
}