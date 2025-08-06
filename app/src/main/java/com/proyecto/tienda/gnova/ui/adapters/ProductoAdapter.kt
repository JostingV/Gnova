package com.proyecto.tienda.gnova.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast // Importa Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.data.models.Producto

class ProductoAdapter(
    private var productos: List<Producto>, // Cambiado a 'var' para poder actualizar la lista
    private val onItemClick: (Producto) -> Unit,
    private val onAgregarClick: (Producto) -> Unit // Este callback se usará para agregar al carrito
) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProducto: ImageView = itemView.findViewById(R.id.imgProducto)
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
        val btnAgregar: Button = itemView.findViewById(R.id.btnAgregarCarrito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productos[position]
        holder.txtNombre.text = producto.nombre
        holder.txtPrecio.text = holder.itemView.context.getString(R.string.formato_precio, producto.precio) // Usar recurso de cadena

        // Carga dinámica de imagen desde URL
        Glide.with(holder.itemView.context)
            .load(producto.imagenUrl)
            .placeholder(R.drawable.logo) // puedes usar un recurso temporal
            .error(R.drawable.logo) // Imagen de error si falla la carga
            .into(holder.imgProducto)

        holder.itemView.setOnClickListener { onItemClick(producto) }
        holder.btnAgregar.setOnClickListener {
            // Aquí se llama a la función onAgregarClick que ahora agregará al Carrito
            onAgregarClick(producto)
            Toast.makeText(holder.itemView.context, holder.itemView.context.getString(R.string.msg_agregado_carrito, producto.nombre), Toast.LENGTH_SHORT).show() // Usar recurso de cadena
        }
    }

    override fun getItemCount() = productos.size

    /**
     * Actualiza la lista de productos del adaptador y notifica los cambios.
     * @param nuevaLista La nueva lista de productos.
     */
    fun actualizarProductos(nuevaLista: List<Producto>) {
        productos = nuevaLista
        notifyDataSetChanged() // Para simplificar, usamos notifyDataSetChanged. Considerar DiffUtil para optimización.
    }
}
