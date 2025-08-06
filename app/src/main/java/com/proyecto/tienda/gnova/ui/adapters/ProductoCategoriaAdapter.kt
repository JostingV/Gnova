package com.proyecto.tienda.gnova.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.data.models.Producto

class ProductoCategoriaAdapter(
    private val productos: List<String>,
    private val onAgregarCarrito: (String) -> Unit
) : RecyclerView.Adapter<ProductoCategoriaAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvNombre: TextView = view.findViewById(R.id.tvNombreProducto)
        private val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        private val btnAgregar: Button = view.findViewById(R.id.btnAgregarCarrito)

        fun bind(producto: String) {
            tvNombre.text = producto
            tvPrecio.text = "Precio: $20" // esto deberías cargarlo dinámico también

            btnAgregar.setOnClickListener {
                onAgregarCarrito(producto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_categoria, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount(): Int = productos.size
}
