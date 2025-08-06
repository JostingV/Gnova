package com.proyecto.tienda.gnova.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.data.models.CategoriaItem

class CategoriaAdapter(
    private val categorias: List<CategoriaItem>,
    private val onItemClick: (CategoriaItem) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    class CategoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.ivCategoria)
        val nombre: TextView = view.findViewById(R.id.tvNombreCategoria)
        val container: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.imagen.setImageResource(categoria.imagen)
        holder.nombre.text = categoria.nombre

        holder.container.setOnClickListener {
            onItemClick(categoria)
        }
    }

    override fun getItemCount() = categorias.size
}