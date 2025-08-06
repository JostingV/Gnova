package com.proyecto.tienda.gnova

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ProductoCrudAdapter(
    private var productos: List<Producto>, // Cambiado a 'var' para poder actualizar la lista
    private val onEditar: (Producto) -> Unit,
    private val onEliminar: (String) -> Unit // El callback ahora recibe un String (el ID del producto)
) : RecyclerView.Adapter<ProductoCrudAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProducto)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)

        fun bind(producto: Producto) {
            tvNombre.text = producto.nombre
            btnEditar.setOnClickListener { onEditar(producto) }
            btnEliminar.setOnClickListener { onEliminar(producto.id) } // Pasar el ID del producto
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_admin, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productos[position])
    }

    override fun getItemCount() = productos.size

    /**
     * Actualiza la lista de productos del adaptador y notifica los cambios.
     * @param nuevaLista La nueva lista de productos.
     */
    fun actualizarProductos(nuevaLista: List<Producto>) {
        productos = nuevaLista
        notifyDataSetChanged() // Por ahora, se usa notifyDataSetChanged para simplicidad.
        // Para un rendimiento óptimo en listas grandes, se usaría DiffUtil.
    }
}
