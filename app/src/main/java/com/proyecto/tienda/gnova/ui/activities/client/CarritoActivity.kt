package com.proyecto.tienda.gnova.ui.activities.client

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.data.models.Carrito
import com.proyecto.tienda.gnova.ui.adapters.CarritoAdapter
import com.proyecto.tienda.gnova.ui.activities.shared.DetalleProductoActivity

class CarritoActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var btnFinalizar: Button
    private lateinit var tvTotal: TextView
    private lateinit var tvCantidadItems: TextView
    private lateinit var adapter: CarritoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        initViews()
        setupRecycler()
        actualizarUI()
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        actualizarUI()
    }

    private fun initViews() {
        recycler = findViewById(R.id.recyclerCarrito)
        btnFinalizar = findViewById(R.id.btnFinalizarCompra)
        tvTotal = findViewById(R.id.tvTotal)
        tvCantidadItems = findViewById(R.id.tvCantidadItems)
    }

    private fun setupRecycler() {
        recycler.layoutManager = LinearLayoutManager(this)
        
        adapter = CarritoAdapter(
            items = Carrito.obtenerItems(),
            onItemClick = { item ->
                val intent = Intent(this, DetalleProductoActivity::class.java)
                intent.putExtra("producto", item.producto)
                startActivity(intent)
            },
            onCantidadCambiada = { item, nuevaCantidad ->
                Carrito.actualizarCantidad(item.producto, nuevaCantidad)
                actualizarUI()
            },
            onEliminarItem = { item ->
                Carrito.eliminarItemCompleto(item.producto)
                actualizarUI()
                Toast.makeText(this, "${item.producto.nombre} eliminado del carrito", Toast.LENGTH_SHORT).show()
            }
        )
        
        recycler.adapter = adapter
    }

    private fun setupButtons() {
        btnFinalizar.setOnClickListener {
            if (Carrito.obtenerItems().isEmpty()) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            Carrito.limpiarCarrito()
            Toast.makeText(this, getString(R.string.msg_compra_realizada), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun actualizarUI() {
        val items = Carrito.obtenerItems()
        adapter.actualizarItems(items)
        
        // Actualizar totales
        val cantidadTotal = Carrito.obtenerCantidadTotal()
        val precioTotal = Carrito.obtenerTotal()
        
        tvCantidadItems.text = if (cantidadTotal == 1) {
            "$cantidadTotal artículo"
        } else {
            "$cantidadTotal artículos"
        }
        
        tvTotal.text = "$${String.format("%.2f", precioTotal)}"
        
        // Habilitar/deshabilitar botón finalizar
        btnFinalizar.isEnabled = items.isNotEmpty()
        btnFinalizar.alpha = if (items.isNotEmpty()) 1.0f else 0.5f
    }
}