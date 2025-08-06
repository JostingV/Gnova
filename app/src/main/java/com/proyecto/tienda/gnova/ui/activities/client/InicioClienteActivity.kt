package com.proyecto.tienda.gnova.ui.activities.client

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.ui.adapters.CategoriaAdapter
import com.proyecto.tienda.gnova.ui.adapters.ProductoAdapter
import com.proyecto.tienda.gnova.ui.activities.shared.BusquedaActivity
import com.proyecto.tienda.gnova.ui.activities.shared.DetalleProductoActivity
import com.proyecto.tienda.gnova.ui.activities.shared.ProductosPorCategoriaActivity
import com.proyecto.tienda.gnova.data.models.CategoriaItem
import com.proyecto.tienda.gnova.data.models.Carrito
import com.proyecto.tienda.gnova.data.repositories.ProductoRepositorio

class InicioClienteActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerCategorias: RecyclerView
    private lateinit var ivBuscar: ImageView
    private lateinit var varCarrito: ImageView
    private lateinit var recyclerProductosRecientes: RecyclerView // Nuevo RecyclerView para productos recientes

    private var generoSeleccionado = "Hombre" // Género por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio_cliente)

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        tabLayout = findViewById(R.id.tabLayoutGenero)
        recyclerCategorias = findViewById(R.id.recyclerCategorias)
        ivBuscar = findViewById(R.id.ivBuscar)
        varCarrito = findViewById(R.id.ivCarrito)
        recyclerProductosRecientes = findViewById(R.id.recyclerProductosRecientes)

        // Configurar Toolbar y Navigation Drawer
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        // Configurar TabLayout para seleccionar género
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.genero_hombre))) // Usar getString()
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.genero_mujer)))  // Usar getString()
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.genero_nino)))   // Usar getString()
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.genero_nina)))   // Usar getString()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                generoSeleccionado = tab?.text.toString()
                actualizarCategoriasYProductos()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Configurar RecyclerView de categorías inicialmente para "Hombre"
        recyclerCategorias.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        actualizarCategoriasYProductos() // Carga inicial de categorías y productos

        // Configurar RecyclerView de productos recientes (o destacados)
        recyclerProductosRecientes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        observarProductosRecientes()

        // Configurar listeners para iconos de búsqueda y carrito
        ivBuscar.setOnClickListener {
            startActivity(Intent(this, BusquedaActivity::class.java))
        }
        varCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        // Iniciar la escucha de productos de Firestore
        ProductoRepositorio.iniciarEscuchaProductos()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener la escucha de productos de Firestore al destruir la actividad
        ProductoRepositorio.detenerEscuchaProductos()
    }

    private fun actualizarCategoriasYProductos() {
        val categorias = obtenerCategoriasPorGenero(generoSeleccionado)
        recyclerCategorias.adapter = CategoriaAdapter(categorias) { categoria ->
            val intent = Intent(this, ProductosPorCategoriaActivity::class.java)
            intent.putExtra("categoria", categoria.nombre)
            intent.putExtra("genero", generoSeleccionado) // Pasar también el género
            startActivity(intent)
        }
        // No es necesario actualizar productos recientes aquí, ya que se observan automáticamente.
    }

    private fun observarProductosRecientes() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ProductoRepositorio.productos.collect { productos ->
                    // Filtrar por género si es necesario, o mostrar los más recientes de todos
                    val productosFiltrados = productos.filter { it.genero == generoSeleccionado || generoSeleccionado == "Hombre" } // Mostrar productos sin género si no se especifica
                        .sortedByDescending { it.id } // Ordenar por ID para simular "más recientes"
                        .take(10) // Tomar los 10 más recientes
                    recyclerProductosRecientes.adapter = ProductoAdapter(
                        productos = productosFiltrados,
                        onItemClick = { producto ->
                            val intent = Intent(this@InicioClienteActivity, DetalleProductoActivity::class.java)
                            intent.putExtra("producto", producto)
                            startActivity(intent)
                        },
                        onAgregarClick = { producto ->
                            Carrito.agregarProducto(producto)
                            Toast.makeText(this@InicioClienteActivity, getString(R.string.msg_agregado_carrito, producto.nombre), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    private fun obtenerCategoriasPorGenero(genero: String): List<CategoriaItem> {
        return when (genero) {
            "Hombre" -> listOf(
                CategoriaItem(getString(R.string.categoria_novedades), R.drawable.categoria_novedades_hombre),
                CategoriaItem(getString(R.string.categoria_zapatillas), R.drawable.categoria_zapatillas_hombre),
                CategoriaItem(getString(R.string.categoria_ropa), R.drawable.categoria_ropa_hombre),
                CategoriaItem(getString(R.string.categoria_accesorios), R.drawable.categoria_accesorios_hombre)
            )
            "Mujer" -> listOf(
                CategoriaItem(getString(R.string.categoria_novedades), R.drawable.categoria_novedades_mujer),
                CategoriaItem(getString(R.string.categoria_zapatillas), R.drawable.categoria_zapatillas_mujer),
                CategoriaItem(getString(R.string.categoria_ropa), R.drawable.categoria_ropa_mujer),
                CategoriaItem(getString(R.string.categoria_accesorios), R.drawable.categoria_accesorios_mujer)
            )
            "Niño" -> listOf(
                CategoriaItem(getString(R.string.categoria_novedades), R.drawable.categoria_novedades_nino),
                CategoriaItem(getString(R.string.categoria_zapatillas), R.drawable.categoria_zapatillas_nino),
                CategoriaItem(getString(R.string.categoria_ropa), R.drawable.categoria_ropa_nino),
                CategoriaItem(getString(R.string.categoria_accesorios), R.drawable.categoria_accesorios_nino)
            )
            "Niña" -> listOf(
                CategoriaItem(getString(R.string.categoria_novedades), R.drawable.categoria_novedades_nina),
                CategoriaItem(getString(R.string.categoria_zapatillas), R.drawable.categoria_zapatillas_nina),
                CategoriaItem(getString(R.string.categoria_ropa), R.drawable.categoria_ropa_nina),
                CategoriaItem(getString(R.string.categoria_accesorios), R.drawable.categoria_accesorios_nina)
            )
            else -> listOf( // Default si no se especifica género
                CategoriaItem(getString(R.string.categoria_novedades), R.drawable.categoria_novedades),
                CategoriaItem(getString(R.string.categoria_zapatillas), R.drawable.categoria_zapatillas),
                CategoriaItem(getString(R.string.categoria_ropa), R.drawable.categoria_ropa),
                CategoriaItem(getString(R.string.categoria_accesorios), R.drawable.categoria_accesorios)
            )
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_perfil -> {
                startActivity(Intent(this, PerfilClienteActivity::class.java))
            }
            R.id.nav_favoritos -> {
                startActivity(Intent(this, FavoritosActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @Suppress("DEPRECATION") // Suprime
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
