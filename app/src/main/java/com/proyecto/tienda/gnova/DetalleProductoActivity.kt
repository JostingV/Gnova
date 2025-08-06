package com.proyecto.tienda.gnova

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.util.Locale

class DetalleProductoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_producto)

        // Manejar la recuperación de Parcelable (Producto) del Intent
        val producto: Producto? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("producto", Producto::class.java) // Usar getParcelableExtra
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("producto") // Usar getParcelableExtra (deprecated para API < 33)
        }

        producto?.let { productoObjeto -> // Se renombra 'it' a 'productoObjeto' para evitar ambigüedades
            findViewById<TextView>(R.id.tvNombreProducto).text = productoObjeto.nombre
            // Usar String.format con Locale y recursos de cadena para el precio
            findViewById<TextView>(R.id.tvPrecio).text =
                getString(R.string.detalle_precio, String.format(Locale.getDefault(), "%.2f", productoObjeto.precio))
            findViewById<TextView>(R.id.tvDescripcion).text = productoObjeto.descripcion
            // Usar recursos de cadena con placeholders para evitar concatenación y problemas de traducción
            findViewById<TextView>(R.id.tvTalla).text =
                getString(R.string.detalle_talla, productoObjeto.talla)
            findViewById<TextView>(R.id.tvCategoria).text =
                getString(R.string.detalle_categoria, productoObjeto.categoria)
            findViewById<TextView>(R.id.tvGenero).text =
                getString(R.string.detalle_genero, productoObjeto.genero)
            findViewById<TextView>(R.id.tvStock).text =
                getString(R.string.detalle_stock, productoObjeto.stock) // Acceso correcto a stock


            val imgProducto: ImageView = findViewById(R.id.imgProductoDetalle)
            Glide.with(this)
                .load(productoObjeto.imagenUrl)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(imgProducto)


            findViewById<Button>(R.id.btnAgregarCarrito).setOnClickListener {
                if (productoObjeto.stock > 0) { // Comprobación de stock
                    Carrito.agregarProducto(productoObjeto) // Se pasa productoObjeto
                    Toast.makeText(
                        this,
                        getString(R.string.msg_agregado_carrito, productoObjeto.nombre), // Usar recurso de cadena
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.msg_sin_stock, productoObjeto.nombre), // Usar recurso de cadena
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            findViewById<Button>(R.id.btnAgregarFavorito).setOnClickListener {
                ProductoRepositorio.agregarAFavoritos(productoObjeto) // Se pasa productoObjeto
                Toast.makeText(
                    this,
                    getString(R.string.msg_agregado_favoritos, productoObjeto.nombre), // Usar recurso de cadena
                    Toast.LENGTH_SHORT
                ).show()
            }
            // Asegúrate de que este botón exista en tu activity_detalle_producto.xml con el ID correcto
            findViewById<Button>(R.id.btnEliminarDelCarrito)?.setOnClickListener {
                if (Carrito.obtenerProductos().contains(productoObjeto)) {
                    Carrito.eliminarProducto(productoObjeto)
                    Toast.makeText(
                        this,
                        getString(R.string.msg_eliminado_carrito, productoObjeto.nombre),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.msg_no_en_carrito, productoObjeto.nombre),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } ?: run {
            Toast.makeText(this, getString(R.string.error_producto_no_encontrado), Toast.LENGTH_SHORT).show() // Usar recurso de cadena
            finish()
        }
    }
}
