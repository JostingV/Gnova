package com.proyecto.tienda.gnova

import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BusquedaActivity : AppCompatActivity() {

    private lateinit var etBusqueda: EditText
    private lateinit var ivVolver: ImageView
    private lateinit var recyclerResultados: RecyclerView
    private lateinit var adapter: ProductoAdapter // Declarar el adaptador como propiedad de la clase

    // Flow para el texto de búsqueda y el trabajo de la corrutina
    private val searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busqueda)

        initViews()
        setupViews()
        setupSearchLogic()
    }

    private fun initViews() {
        etBusqueda = findViewById(R.id.etBusqueda)
        ivVolver = findViewById(R.id.ivVolver)
        recyclerResultados = findViewById(R.id.recyclerResultados)
    }

    private fun setupViews() {
        // Configurar botón de volver
        ivVolver.setOnClickListener {
            finish()
            // Evitar overridePendingTransition obsoleto si es posible, o usar una alternativa más nueva si se tiene
            // overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        // Configurar RecyclerView
        recyclerResultados.layoutManager = LinearLayoutManager(this)
        adapter = ProductoAdapter(
            productos = emptyList(), // Inicializar con una lista vacía
            onItemClick = { producto ->
                val intent = Intent(this, DetalleProductoActivity::class.java)
                intent.putExtra("producto", producto)
                startActivity(intent)
            },
            onAgregarClick = { producto ->
                if (producto.stock > 0) {
                    Carrito.agregarProducto(producto)
                    Toast.makeText(this, getString(R.string.msg_agregado_carrito, producto.nombre), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.msg_sin_stock, producto.nombre), Toast.LENGTH_SHORT).show()
                }
            }
        )
        recyclerResultados.adapter = adapter

        // Enfocar automáticamente el campo de búsqueda
        etBusqueda.requestFocus()
    }

    private fun setupSearchLogic() {
        // Configurar búsqueda en tiempo real
        etBusqueda.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Actualizar el Flow de búsqueda con el texto actual
                searchQuery.value = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Observar los cambios en el texto de búsqueda y en la lista de productos
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchQuery
                    .collectLatest { query ->
                        // Cancelar cualquier búsqueda anterior para evitar resultados desactualizados
                        searchJob?.cancel()
                        if (query.isNotEmpty()) {
                            // Iniciar una nueva corrutina para la búsqueda con un pequeño delay (debounce)
                            searchJob = launch {
                                delay(300) // Pequeño retraso para evitar búsquedas excesivas
                                // Recopilar los productos del repositorio y filtrar
                                ProductoRepositorio.productos.collect { todosLosProductos ->
                                    val productosEncontrados = todosLosProductos.filter { producto ->
                                        // Realizar la búsqueda por nombre, descripción o categoría (insensible a mayúsculas/minúsculas)
                                        producto.nombre.contains(query, ignoreCase = true) ||
                                                producto.descripcion.contains(query, ignoreCase = true) ||
                                                producto.categoria.contains(query, ignoreCase = true)
                                    }
                                    adapter.actualizarProductos(productosEncontrados)
                                    // Opcional: Mostrar mensaje "No hay resultados" si la lista está vacía
                                    // findViewById<TextView>(R.id.tvNoResultados).visibility =
                                    //     if (productosEncontrados.isEmpty()) View.VISIBLE else View.GONE
                                }
                            }
                        } else {
                            // Limpiar resultados si la búsqueda está vacía
                            adapter.actualizarProductos(emptyList())
                            // findViewById<TextView>(R.id.tvNoResultados).visibility = View.GONE
                        }
                    }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Asegúrate de que la escucha de Firestore esté activa cuando la actividad esté visible
        ProductoRepositorio.iniciarEscuchaProductos()
    }

    override fun onPause() {
        super.onPause()
        // Detén la escucha de Firestore cuando la actividad no esté visible para ahorrar recursos
        ProductoRepositorio.detenerEscuchaProductos()
        // Cancelar el trabajo de búsqueda si la actividad se pausa
        searchJob?.cancel()
    }
}
