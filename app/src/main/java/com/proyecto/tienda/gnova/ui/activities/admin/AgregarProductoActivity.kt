package com.proyecto.tienda.gnova.ui.activities.admin

import android.content.Intent
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

class AgregarProductoActivity : AppCompatActivity() {
    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etTalla: EditText
    private lateinit var etStock: EditText
    private lateinit var etImagenUrl: EditText
    private lateinit var spCategoria: Spinner // Spinner para categoría
    private lateinit var spGenero: Spinner   // Spinner para género
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_producto)

        etNombre = findViewById(R.id.etNombreProducto)
        etDescripcion = findViewById(R.id.etDescripcion)
        etPrecio = findViewById(R.id.etPrecioProducto)
        etTalla = findViewById(R.id.etTalla)
        etStock = findViewById(R.id.etStock)
        etImagenUrl = findViewById(R.id.etImagenUrl)
        spCategoria = findViewById(R.id.spCategoriaProducto) // Asumiendo ID spCategoriaProducto
        spGenero = findViewById(R.id.spGeneroProducto)     // Asumiendo ID spGeneroProducto
        btnGuardar = findViewById(R.id.btnGuardarProducto)

        // Configurar adaptador para el Spinner de categorías
        ArrayAdapter.createFromResource(
            this,
            R.array.categorias_array, // Asegúrate de que este array esté definido en res/values/strings.xml
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCategoria.adapter = adapter
        }

        // Configurar adaptador para el Spinner de géneros
        ArrayAdapter.createFromResource(
            this,
            R.array.generos_array, // Asegúrate de que este array esté definido en res/values/strings.xml
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spGenero.adapter = adapter
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
            val talla = etTalla.text.toString().trim()
            val stock = etStock.text.toString().toIntOrNull() ?: 0
            val categoria = spCategoria.selectedItem.toString()
            val imagenUrl = etImagenUrl.text.toString().trim()
            val genero = spGenero.selectedItem.toString() // Obtener el género seleccionado

            // Validaciones básicas
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


            val nuevoProducto = Producto(
                // Firestore generará el ID si lo dejas vacío al añadirlo.
                // Sin embargo, si necesitas el ID antes de que se agregue a Firestore,
                // podrías generarlo aquí (ej. UUID.randomUUID().toString())
                // pero generalmente Firestore lo maneja.
                id = "", // ID se generará en Firestore si es un nuevo producto
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                talla = talla,
                stock = stock,
                categoria = categoria,
                imagenUrl = imagenUrl,
                genero = genero // Incluir el género
            )

            ProductoRepositorio.agregarProducto(
                nuevoProducto,
                onSuccess = { productId ->
                    Toast.makeText(this, getString(R.string.msg_guardado_exito), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ListaProductosAdminActivity::class.java))
                    finish()
                },
                onFailure = { e ->
                    Toast.makeText(this, "Error al guardar producto: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
