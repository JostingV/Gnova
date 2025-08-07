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
            R.array.categorias_array,
            R.layout.spinner_selected_item // Layout para el item seleccionado
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item) // Layout para dropdown
            spCategoria.adapter = adapter
        }

        // Configurar adaptador para el Spinner de géneros
        ArrayAdapter.createFromResource(
            this,
            R.array.generos_array,
            R.layout.spinner_selected_item // Layout para el item seleccionado
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item) // Layout para dropdown
            spGenero.adapter = adapter
        }

        // Agregar listeners para verificar las selecciones
        spCategoria.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val categoriaSeleccionada = parent?.getItemAtPosition(position).toString()
                android.util.Log.d("AgregarProducto", "Categoría seleccionada: $categoriaSeleccionada")
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        spGenero.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val generoSeleccionado = parent?.getItemAtPosition(position).toString()
                android.util.Log.d("AgregarProducto", "Género seleccionado: $generoSeleccionado")
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val precioTexto = etPrecio.text.toString().trim()
            val talla = etTalla.text.toString().trim()
            val stockTexto = etStock.text.toString().trim()
            val categoria = spCategoria.selectedItem?.toString() ?: ""
            val imagenUrl = etImagenUrl.text.toString().trim()
            val genero = spGenero.selectedItem?.toString() ?: ""

            // Log de depuración para identificar problemas
            android.util.Log.d("AgregarProducto", "=== DATOS DEL FORMULARIO ===")
            android.util.Log.d("AgregarProducto", "Nombre: '$nombre'")
            android.util.Log.d("AgregarProducto", "Descripción: '$descripcion'")
            android.util.Log.d("AgregarProducto", "Precio texto: '$precioTexto'")
            android.util.Log.d("AgregarProducto", "Talla: '$talla'")
            android.util.Log.d("AgregarProducto", "Stock texto: '$stockTexto'")
            android.util.Log.d("AgregarProducto", "Categoría: '$categoria'")
            android.util.Log.d("AgregarProducto", "Género: '$genero'")
            android.util.Log.d("AgregarProducto", "ImagenUrl: '$imagenUrl'")

            // Validaciones específicas con mensajes claros
            if (nombre.isEmpty()) {
                Toast.makeText(this, "❌ El nombre del producto es obligatorio", Toast.LENGTH_LONG).show()
                etNombre.requestFocus()
                android.util.Log.e("AgregarProducto", "Error: Nombre vacío")
                return@setOnClickListener
            }

            if (descripcion.isEmpty()) {
                Toast.makeText(this, "❌ La descripción es obligatoria", Toast.LENGTH_LONG).show()
                etDescripcion.requestFocus()
                android.util.Log.e("AgregarProducto", "Error: Descripción vacía")
                return@setOnClickListener
            }

            if (precioTexto.isEmpty()) {
                Toast.makeText(this, "❌ El precio es obligatorio", Toast.LENGTH_LONG).show()
                etPrecio.requestFocus()
                android.util.Log.e("AgregarProducto", "Error: Precio vacío")
                return@setOnClickListener
            }

            val precio = try {
                precioTexto.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "❌ El precio debe ser un número válido", Toast.LENGTH_LONG).show()
                etPrecio.requestFocus()
                android.util.Log.e("AgregarProducto", "Error al convertir precio: $precioTexto", e)
                return@setOnClickListener
            }

            if (precio <= 0) {
                Toast.makeText(this, "❌ El precio debe ser mayor que 0", Toast.LENGTH_LONG).show()
                etPrecio.requestFocus()
                android.util.Log.e("AgregarProducto", "Error: Precio <= 0: $precio")
                return@setOnClickListener
            }

            if (talla.isEmpty()) {
                Toast.makeText(this, "❌ La talla es obligatoria", Toast.LENGTH_LONG).show()
                etTalla.requestFocus()
                android.util.Log.e("AgregarProducto", "Error: Talla vacía")
                return@setOnClickListener
            }

            if (stockTexto.isEmpty()) {
                Toast.makeText(this, "❌ El stock es obligatorio", Toast.LENGTH_LONG).show()
                etStock.requestFocus()
                android.util.Log.e("AgregarProducto", "Error: Stock vacío")
                return@setOnClickListener
            }

            val stock = try {
                stockTexto.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "❌ El stock debe ser un número entero", Toast.LENGTH_LONG).show()
                etStock.requestFocus()
                android.util.Log.e("AgregarProducto", "Error al convertir stock: $stockTexto", e)
                return@setOnClickListener
            }

            if (stock < 0) {
                Toast.makeText(this, "❌ El stock no puede ser negativo", Toast.LENGTH_LONG).show()
                etStock.requestFocus()
                android.util.Log.e("AgregarProducto", "Error: Stock negativo: $stock")
                return@setOnClickListener
            }

            if (categoria.isEmpty() || categoria == "Seleccionar categoría") {
                Toast.makeText(this, "❌ Selecciona una categoría", Toast.LENGTH_LONG).show()
                android.util.Log.e("AgregarProducto", "Error: Categoría no seleccionada")
                return@setOnClickListener
            }

            if (genero.isEmpty() || genero == "Seleccionar género") {
                Toast.makeText(this, "❌ Selecciona el género del producto", Toast.LENGTH_LONG).show()
                android.util.Log.e("AgregarProducto", "Error: Género no seleccionado")
                return@setOnClickListener
            }

            // Crear el producto
            val nuevoProducto = Producto(
                id = "", // ID se generará en Firestore
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                talla = talla,
                stock = stock,
                categoria = categoria,
                imagenUrl = imagenUrl,
                genero = genero
            )

            android.util.Log.d("AgregarProducto", "Intentando guardar producto: $nuevoProducto")

            // Guardar en Firestore
            ProductoRepositorio.agregarProducto(
                nuevoProducto,
                onSuccess = { productId ->
                    android.util.Log.d("AgregarProducto", "✅ Producto guardado exitosamente con ID: $productId")
                    Toast.makeText(this, "✅ Producto guardado exitosamente", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ListaProductosAdminActivity::class.java))
                    finish()
                },
                onFailure = { exception ->
                    android.util.Log.e("AgregarProducto", "❌ Error al guardar producto", exception)
                    android.util.Log.e("AgregarProducto", "Tipo de error: ${exception.javaClass.simpleName}")
                    android.util.Log.e("AgregarProducto", "Mensaje: ${exception.message}")
                    android.util.Log.e("AgregarProducto", "Causa: ${exception.cause}")
                    
                    val mensaje = when {
                        exception.message?.contains("PERMISSION_DENIED") == true -> 
                            "❌ Error de permisos en Firestore. Verifica la configuración."
                        exception.message?.contains("network") == true -> 
                            "❌ Error de conexión. Verifica tu internet."
                        else -> 
                            "❌ Error al guardar: ${exception.message}"
                    }
                    
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
