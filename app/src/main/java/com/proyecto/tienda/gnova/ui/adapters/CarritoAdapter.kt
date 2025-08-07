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
import com.proyecto.tienda.gnova.data.models.ItemCarrito

class CarritoAdapter(
    private var items: List<ItemCarrito>,
    private val onItemClick: (ItemCarrito) -> Unit,
    private val onCantidadCambiada: (ItemCarrito, Int) -> Unit,
    private val onEliminarItem: (ItemCarrito) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    class CarritoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.imgProducto)
        val nombre: TextView = view.findViewById(R.id.txtNombre)
        val precio: TextView = view.findViewById(R.id.txtPrecio)
        val cantidad: TextView = view.findViewById(R.id.tvCantidad)
        val subtotal: TextView = view.findViewById(R.id.tvSubtotal)
        val btnMenos: Button = view.findViewById(R.id.btnMenos)
        val btnMas: Button = view.findViewById(R.id.btnMas)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = items[position]
        val producto = item.producto
        
        holder.nombre.text = producto.nombre
        holder.precio.text = String.format("$%.2f", producto.precio)
        holder.cantidad.text = item.cantidad.toString()
        holder.subtotal.text = String.format("$%.2f", producto.precio * item.cantidad)
        
        // Cargar imagen
        Glide.with(holder.itemView.context)
            .load(producto.imagenUrl)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo)
            .into(holder.imagen)

        // Click en el item completo para ver detalle
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        // Botón menos cantidad
        holder.btnMenos.setOnClickListener {
            if (item.cantidad > 1) {
                onCantidadCambiada(item, item.cantidad - 1)
            }
        }

        // Botón más cantidad
        holder.btnMas.setOnClickListener {
            onCantidadCambiada(item, item.cantidad + 1)
        }

        // Botón eliminar
        holder.btnEliminar.setOnClickListener {
            onEliminarItem(item)
        }

        // Deshabilitar botón menos si cantidad es 1
        holder.btnMenos.isEnabled = item.cantidad > 1
        holder.btnMenos.alpha = if (item.cantidad > 1) 1.0f else 0.5f
    }

    override fun getItemCount(): Int = items.size

    fun actualizarItems(nuevosItems: List<ItemCarrito>) {
        items = nuevosItems
        notifyDataSetChanged()
    }
}
