package com.proyecto.tienda.gnova.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.data.models.Producto

class ProductoCrudAdapter(
    private var productos: List<Producto>, // Cambiado a 'var' para poder actualizar la lista
    private val onEditar: (Producto) -> Unit,
    private val onEliminar: (String) -> Unit // El callback ahora recibe un String (el ID del producto)
) : RecyclerView.Adapter<ProductoCrudAdapter.ViewHolder>() {

    private var productosFiltrados: List<Producto> = productos // Lista filtrada

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProducto)
        val imgProducto: android.widget.ImageView = view.findViewById(R.id.imgProducto)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)

        fun bind(producto: Producto) {
            tvNombre.text = producto.nombre
            // Cargar imagen con Glide
            com.bumptech.glide.Glide.with(itemView.context)
                .load(producto.imagenUrl)
                .placeholder(com.proyecto.tienda.gnova.R.drawable.logo)
                .error(com.proyecto.tienda.gnova.R.drawable.logo)
                .into(imgProducto)
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
        holder.bind(productosFiltrados[position])
    }

    override fun getItemCount() = productosFiltrados.size

    /**
     * Actualiza la lista de productos del adaptador y notifica los cambios.
     * @param nuevaLista La nueva lista de productos.
     */
    fun actualizarProductos(nuevaLista: List<Producto>) {
        productos = nuevaLista
        productosFiltrados = nuevaLista // Actualizamos la lista filtrada con todos los productos
        notifyDataSetChanged() // Por ahora, se usa notifyDataSetChanged para simplicidad.
        // Para un rendimiento óptimo en listas grandes, se usaría DiffUtil.
    }

    /**
     * Filtra los productos según el texto ingresado en el SearchView.
     * @param query El texto de búsqueda.
     */
    fun filtrarProductos(query: String) {
        productosFiltrados = if (query.isEmpty()) {
            productos // Si no hay texto en la búsqueda, mostrar todos los productos
        } else {
            productos.filter { producto ->
                producto.nombre.contains(query, ignoreCase = true) ||
                        producto.categoria.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged() // Actualizar la vista del RecyclerView
    }
}
