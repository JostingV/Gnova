package com.proyecto.tienda.gnova.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.data.models.Producto

class ProductoGridAdapter(
    private var productos: List<Producto>,
    private val onItemClick: (Producto) -> Unit,
    private val onAgregarClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoGridAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.ivProductoImagen)
        val nombre: TextView = view.findViewById(R.id.tvProductoNombre)
        val precio: TextView = view.findViewById(R.id.tvProductoPrecio)
        val btnVerDetalle: Button = view.findViewById(R.id.btnVerDetalle)
        val btnAgregarCarrito: Button = view.findViewById(R.id.btnAgregarCarrito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_grid, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        
        holder.nombre.text = producto.nombre
        holder.precio.text = String.format("$%.2f", producto.precio)
        
        // Cargar imagen con Glide
        Glide.with(holder.itemView.context)
            .load(producto.imagenUrl)
            .placeholder(R.drawable.logo) // Placeholder mientras carga
            .error(R.drawable.logo) // Imagen de error si falla la carga
            .into(holder.imagen)

        // Click en "Ver" - va al detalle
        holder.btnVerDetalle.setOnClickListener {
            onItemClick(producto)
        }

        // Click en "Agregar" - agrega al carrito
        holder.btnAgregarCarrito.setOnClickListener {
            onAgregarClick(producto)
        }

        // Click en toda la card también va al detalle
        holder.itemView.setOnClickListener {
            onItemClick(producto)
        }
    }

    override fun getItemCount(): Int = productos.size

    fun actualizarProductos(nuevosProductos: List<Producto>) {
        productos = nuevosProductos
        notifyDataSetChanged()
    }
}
