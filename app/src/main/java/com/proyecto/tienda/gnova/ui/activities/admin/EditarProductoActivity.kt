package com.proyecto.tienda.gnova.ui.activities.admin

import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.data.models.Producto
import com.proyecto.tienda.gnova.data.repositories.ProductoRepositorio

class EditarProductoActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etTalla: EditText
    private lateinit var etStock: EditText
    private lateinit var etImagenUrl: EditText
    private lateinit var spCategoria: Spinner
    private lateinit var spGenero: Spinner
    private lateinit var btnActualizar: Button
    private lateinit var btnCancelar: Button

    private var productoOriginal: Producto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_producto)

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombreProducto)
        etDescripcion = findViewById(R.id.etDescripcion)
        etPrecio = findViewById(R.id.etPrecioProducto)
        etTalla = findViewById(R.id.etTalla)
        etStock = findViewById(R.id.etStock)
        etImagenUrl = findViewById(R.id.etImagenUrl)
        spCategoria = findViewById(R.id.spCategoriaProducto) // Asegúrate de que el ID es 'spCategoriaProducto' en tu XML
        spGenero = findViewById(R.id.spGeneroProducto)       // Asegúrate de que el ID es 'spGeneroProducto' en tu XML
        btnActualizar = findViewById(R.id.btnActualizarProducto)
        btnCancelar = findViewById(R.id.btnCancelar)

        // Adaptadores para los Spinners
        ArrayAdapter.createFromResource(
            this,
            R.array.categorias_array, // Asegúrate de que este array esté definido en res/values/strings.xml
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCategoria.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.generos_array, // Asegúrate de que este array esté definido en res/values/strings.xml
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spGenero.adapter = adapter
        }

        // Obtener el producto pasado por el Intent, manejando la deprecación y usando getParcelableExtra
        productoOriginal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("producto", Producto::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("producto") // Usar getParcelableExtra (deprecated para API < 33)
        }

        // Cargar datos del producto en los campos de edición
        productoOriginal?.let { producto ->
            etNombre.setText(producto.nombre)
            etDescripcion.setText(producto.descripcion)
            etPrecio.setText(producto.precio.toString())
            etTalla.setText(producto.talla)
            etStock.setText(producto.stock.toString())
            etImagenUrl.setText(producto.imagenUrl)

            // Seleccionar categoría y género en los Spinners
            val categoriasArray = resources.getStringArray(R.array.categorias_array)
            val categoriaIndex = categoriasArray.indexOf(producto.categoria)
            if (categoriaIndex != -1) spCategoria.setSelection(categoriaIndex)

            val generosArray = resources.getStringArray(R.array.generos_array)
            val generoIndex = generosArray.indexOf(producto.genero)
            if (generoIndex != -1) spGenero.setSelection(generoIndex)
        } ?: run {
            Toast.makeText(this, "Error: Producto no encontrado para edición", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Listener para el botón Actualizar
        btnActualizar.setOnClickListener {
            productoOriginal?.let { original ->
                val nombre = etNombre.text.toString().trim()
                val descripcion = etDescripcion.text.toString().trim()
                val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                val talla = etTalla.text.toString().trim()
                val stock = etStock.text.toString().toIntOrNull() ?: 0
                val categoria = spCategoria.selectedItem.toString()
                val genero = spGenero.selectedItem.toString()
                val imagenUrl = etImagenUrl.text.toString().trim()

                if (nombre.isEmpty() || descripcion.isEmpty() || talla.isEmpty() || imagenUrl.isEmpty()) {
                    Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (precio <= 0) {
                    Toast.makeText(this, "El precio debe ser mayor que 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (stock < 0) {
                    Toast.makeText(this, "El stock no puede ser negativo", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val productoActualizado = original.copy(
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    talla = talla,
                    stock = stock,
                    categoria = categoria,
                    imagenUrl = imagenUrl,
                    genero = genero
                )

                ProductoRepositorio.editarProducto(
                    productoActualizado,
                    onSuccess = {
                        Toast.makeText(this, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onFailure = { e ->
                        Toast.makeText(this, "Error al actualizar producto: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }

        // Listener para el botón Cancelar
        btnCancelar.setOnClickListener {
            finish()
        }
    }
}
