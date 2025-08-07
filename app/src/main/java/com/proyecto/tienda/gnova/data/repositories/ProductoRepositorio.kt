package com.proyecto.tienda.gnova.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.proyecto.tienda.gnova.data.models.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ProductoRepositorio {

    // Instancia de Firestore
    private val db = Firebase.firestore
    private val productosCollection = db.collection("productos") // Colección de productos en Firestore

    // Live data para la lista de productos (en tiempo real con Flow)
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    // Listener para Firestore
    private var productosListener: ListenerRegistration? = null

    // Lista en memoria para productos favoritos (no persistente en Firestore por simplicidad)
    private val productosFavoritos = mutableListOf<Producto>()

    fun obtenerFavoritos(): List<Producto> = productosFavoritos

    fun agregarAFavoritos(producto: Producto) {
        if (!productosFavoritos.contains(producto)) {
            productosFavoritos.add(producto)
            Log.d("ProductoRepositorio", "${producto.nombre} agregado a favoritos.")
        } else {
            Log.d("ProductoRepositorio", "${producto.nombre} ya está en favoritos.")
        }
    }

    fun eliminarDeFavoritos(producto: Producto) {
        productosFavoritos.remove(producto)
        Log.d("ProductoRepositorio", "${producto.nombre} eliminado de favoritos.")
    }

    /**
     * Inicia la escucha de productos desde Firestore en tiempo real.
     * Esta función debe ser llamada cuando la Activity/Fragment esté en estado STARTED o RESUMED.
     * Se recomienda llamarla en onResume() o en un lifecycleScope.launch en STARTED.
     */
    fun iniciarEscuchaProductos() {
        if (productosListener == null) { // Evitar listeners duplicados
            productosListener = productosCollection.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("ProductoRepositorio", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val productosList = snapshot.documents.mapNotNull { document ->
                        // Mapear el documento de Firestore a un objeto Producto
                        document.toObject(Producto::class.java)?.copy(id = document.id)
                    }
                    _productos.value = productosList
                    Log.d("ProductoRepositorio", "Productos actualizados: ${productosList.size}")
                } else {
                    Log.d("ProductoRepositorio", "Current data: null")
                }
            }
            Log.d("ProductoRepositorio", "Listener de productos iniciado.")
        }
    }

    /**
     * Detiene la escucha de productos de Firestore.
     * Esta función debe ser llamada cuando la Activity/Fragment esté en estado PAUSED o STOPPED.
     * Se recomienda llamarla en onPause() o en un lifecycleScope.cancel() para detener la corrutina.
     */
    fun detenerEscuchaProductos() {
        productosListener?.remove()
        productosListener = null
        Log.d("ProductoRepositorio", "Listener de productos detenido.")
    }

    /**
     * Agrega un nuevo producto a Firestore.
     * @param producto El objeto Producto a agregar. Si el ID está vacío, Firestore generará uno.
     * @param onSuccess Callback para el éxito de la operación, devuelve el ID del nuevo producto.
     * @param onFailure Callback para el fallo de la operación.
     */
    fun agregarProducto(
        producto: Producto,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "nombre" to producto.nombre,
            "descripcion" to producto.descripcion,
            "precio" to producto.precio,
            "talla" to producto.talla,
            "stock" to producto.stock,
            "categoria" to producto.categoria,
            "imagenUrl" to producto.imagenUrl,
            "genero" to producto.genero // Asegúrate de guardar el género también
        )

        productosCollection.add(data)
            .addOnSuccessListener { documentReference ->
                Log.d("ProductoRepositorio", "Producto agregado con ID: ${documentReference.id}")
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { e ->
                Log.w("ProductoRepositorio", "Error al agregar producto", e)
                onFailure(e)
            }
    }

    /**
     * Edita un producto existente en Firestore.
     * @param producto El objeto Producto con los datos actualizados (debe tener un ID válido).
     * @param onSuccess Callback para el éxito de la operación.
     * @param onFailure Callback para el fallo de la operación.
     */
    fun editarProducto(
        producto: Producto,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (producto.id.isEmpty()) {
            onFailure(IllegalArgumentException("El producto debe tener un ID para ser editado."))
            return
        }

        val data = hashMapOf(
            "nombre" to producto.nombre,
            "descripcion" to producto.descripcion,
            "precio" to producto.precio,
            "talla" to producto.talla,
            "stock" to producto.stock,
            "categoria" to producto.categoria,
            "imagenUrl" to producto.imagenUrl,
            "genero" to producto.genero // Asegúrate de actualizar el género también
        )

        productosCollection.document(producto.id).set(data) // set() reemplaza el documento
            .addOnSuccessListener {
                Log.d("ProductoRepositorio", "Producto con ID ${producto.id} actualizado con éxito.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("ProductoRepositorio", "Error al actualizar producto con ID ${producto.id}", e)
                onFailure(e)
            }
    }

    /**
     * Elimina un producto de Firestore.
     * @param productoId El ID del producto a eliminar.
     * @param onSuccess Callback para el éxito de la operación.
     * @param onFailure Callback para el fallo de la operación.
     */
    fun eliminarProducto(
        productoId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        productosCollection.document(productoId).delete()
            .addOnSuccessListener {
                Log.d("ProductoRepositorio", "Producto con ID $productoId eliminado con éxito.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("ProductoRepositorio", "Error al eliminar producto con ID $productoId", e)
                onFailure(e)
            }
    }

    /**
     * Obtiene un producto por su ID de Firestore.
     * @param productoId El ID del producto a buscar.
     * @param onSuccess Callback para el éxito, devuelve el Producto encontrado o null.
     * @param onFailure Callback para el fallo.
     */
    fun obtenerProductoPorId(
        productoId: String,
        onSuccess: (Producto?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        productosCollection.document(productoId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val producto = document.toObject(Producto::class.java)?.copy(id = document.id)
                    onSuccess(producto)
                } else {
                    onSuccess(null) // Producto no encontrado
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    /**
     * Obtiene productos filtrados por categoría y género desde Firestore.
     * Si alguno de los parámetros es null o vacío, no se filtra por ese campo.
     */
    fun obtenerProductosFiltrados(
        categoria: String? = null,
        genero: String? = null,
        onResult: (List<Producto>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        var query = productosCollection as com.google.firebase.firestore.Query

        if (!categoria.isNullOrEmpty()) {
            query = query.whereEqualTo("categoria", categoria)
        }
        if (!genero.isNullOrEmpty()) {
            query = query.whereEqualTo("genero", genero)
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                val productosList = snapshot.documents.mapNotNull { document ->
                    document.toObject(Producto::class.java)?.copy(id = document.id)
                }
                onResult(productosList)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    /**
     * Inicializa algunos productos de ejemplo en Firestore.
     * Este método solo debe llamarse una vez para poblar la base de datos.
     */
    fun inicializarProductosEjemplo() {
        val productosEjemplo = listOf(
            // Productos para Hombre
            Producto(
                nombre = "Nike Air Max",
                descripcion = "Zapatillas deportivas para hombre con tecnología Air Max",
                precio = 159.99,
                talla = "42",
                stock = 15,
                categoria = "Zapatillas",
                imagenUrl = "data:image/jpeg;base64,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", // Usará el logo como placeholder
                genero = "Hombre"
            ),

            Producto(
                nombre = "Reloj Digital",
                descripcion = "Reloj deportivo con monitor de actividad",
                precio = 89.99,
                talla = "Única",
                stock = 8,
                categoria = "Accesorios",
                imagenUrl = "", // Usará el logo como placeholder
                genero = "Hombre"
            ),
            Producto(
                nombre = "Chaqueta Nueva Temporada",
                descripcion = "Chaqueta de última moda para hombre",
                precio = 129.99,
                talla = "M",
                stock = 12,
                categoria = "Novedades",
                imagenUrl = "", // Usará el logo como placeholder
                genero = "Hombre"
            ),
            
            // Productos para Mujer
            Producto(
                nombre = "Zapatillas Running Mujer",
                descripcion = "Zapatillas ligeras para running femenino",
                precio = 139.99,
                talla = "38",
                stock = 20,
                categoria = "Zapatillas",
                imagenUrl = "", // Usará el logo como placeholder
                genero = "Mujer"
            ),
            Producto(
                nombre = "Vestido Casual",
                descripcion = "Vestido cómodo para uso diario",
                precio = 69.99,
                talla = "M",
                stock = 18,
                categoria = "Ropa",
                imagenUrl = "", // Usará el logo como placeholder
                genero = "Mujer"
            ),
            Producto(
                nombre = "Bolso de Mano",
                descripcion = "Bolso elegante para ocasiones especiales",
                precio = 79.99,
                talla = "Única",
                stock = 10,
                categoria = "Accesorios",
                imagenUrl = "", // Usará el logo como placeholder
                genero = "Mujer"
            ),
            Producto(
                nombre = "Blusa Tendencia",
                descripcion = "Blusa de última moda para mujer",
                precio = 49.99,
                talla = "S",
                stock = 22,
                categoria = "Novedades",
                imagenUrl = "", // Usará el logo como placeholder
                genero = "Mujer"
            ),

            // Productos para Niño
            Producto(
                nombre = "Zapatillas Niño",
                descripcion = "Zapatillas cómodas para niños activos",
                precio = 59.99,
                talla = "32",
                stock = 30,
                categoria = "Zapatillas",
                imagenUrl = "", // Usará el logo como placeholder
                genero = "Niño"
            ),
            Producto(
                nombre = "Camiseta Niño",
                descripcion = "Camiseta divertida con estampados",
                precio = 19.99,
                talla = "8",
                stock = 35,
                categoria = "Ropa",
                imagenUrl = "", // Usará el logo como placeholder
                genero = "Niño"
            ),

            // Productos para Niña
            Producto(
                nombre = "Zapatillas Niña",
                descripcion = "Zapatillas con brillos para niñas",
                precio = 55.99,
                talla = "30",
                stock = 25,
                categoria = "Zapatillas",
                imagenUrl = "", // Usará el logo como placeholder
                genero = "Niña"
            ),
            Producto(
                nombre = "Vestido Niña",
                descripcion = "Vestido colorido para niñas",
                precio = 39.99,
                talla = "6",
                stock = 28,
                categoria = "Ropa",
                imagenUrl = "", // Usará el logo como placeholder
                genero = "Niña"
            ),
            // HOMBRE

            Producto(
                nombre = "Zapatillas con LED integrados",
                descripcion = "Zapatillas deportivas con luces LED en la suela, perfectas para un look llamativo y moderno.",
                precio = 120.00,
                talla = "42",
                stock = 15,
                categoria = "Novedades",
                imagenUrl = "https://example.com/zapatillas-led.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Camiseta con tecnología antiolor",
                descripcion = "Camiseta de tecnología avanzada que elimina el mal olor y te mantiene fresco durante todo el día.",
                precio = 45.00,
                talla = "L",
                stock = 20,
                categoria = "Novedades",
                imagenUrl = "https://example.com/camiseta-antiolor.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Gafas con bluetooth",
                descripcion = "Gafas de sol y vista con altavoces Bluetooth integrados para escuchar música o recibir llamadas.",
                precio = 150.00,
                talla = "Única",
                stock = 8,
                categoria = "Novedades",
                imagenUrl = "https://example.com/gafas-bluetooth.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Pantalón con calefacción térmica",
                descripcion = "Pantalón que incorpora tecnología de calefacción, ideal para mantener el calor durante el invierno.",
                precio = 130.00,
                talla = "L",
                stock = 12,
                categoria = "Novedades",
                imagenUrl = "https://example.com/pantalon-calefaccion.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Polera ecológica reciclada",
                descripcion = "Camiseta hecha con materiales reciclados, respetuosa con el medio ambiente y de alta calidad.",
                precio = 50.00,
                talla = "M",
                stock = 18,
                categoria = "Novedades",
                imagenUrl = "https://example.com/polera-ecologica.jpg", // URL de ejemplo
                genero = "Hombre"
            ),

// MUJER

            Producto(
                nombre = "Top con LED para yoga nocturno",
                descripcion = "Top deportivo con luces LED integradas, ideal para practicar yoga o ejercicio en la oscuridad.",
                precio = 70.00,
                talla = "M",
                stock = 15,
                categoria = "Novedades",
                imagenUrl = "https://example.com/top-yoga-led.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Leggings con bolsillo secreto",
                descripcion = "Leggings deportivos con un bolsillo oculto para guardar tus pertenencias de forma segura.",
                precio = 40.00,
                talla = "L",
                stock = 25,
                categoria = "Novedades",
                imagenUrl = "https://example.com/leggings-bolsillo.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Mochila solar",
                descripcion = "Mochila con panel solar integrado que te permite cargar dispositivos móviles mientras te desplazas.",
                precio = 120.00,
                talla = "Única",
                stock = 10,
                categoria = "Novedades",
                imagenUrl = "https://example.com/mochila-solar.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Camiseta que cambia de color con el sol",
                descripcion = "Camiseta que cambia de color cuando se expone al sol, perfecta para un look dinámico.",
                precio = 60.00,
                talla = "S",
                stock = 18,
                categoria = "Novedades",
                imagenUrl = "https://example.com/camiseta-cambia-color.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Gafas con protección UV y altavoz",
                descripcion = "Gafas de sol que ofrecen protección UV y altavoces Bluetooth para escuchar música sin cables.",
                precio = 150.00,
                talla = "Única",
                stock = 8,
                categoria = "Novedades",
                imagenUrl = "https://example.com/gafas-uv-altavoz.jpg", // URL de ejemplo
                genero = "Mujer"
            ),

// NIÑO

            Producto(
                nombre = "Zapatillas con luces y sonidos",
                descripcion = "Zapatillas deportivas para niños que tienen luces LED y sonidos divertidos en cada paso.",
                precio = 45.00,
                talla = "30",
                stock = 20,
                categoria = "Novedades",
                imagenUrl = "https://example.com/zapatillas-luces-sonidos.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Camiseta con diseño cambiante (tinta mágica)",
                descripcion = "Camiseta con diseño que cambia de color con el calor, ideal para niños curiosos.",
                precio = 25.00,
                talla = "8",
                stock = 30,
                categoria = "Novedades",
                imagenUrl = "https://example.com/camiseta-tinta-magica.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Gorro con auriculares bluetooth",
                descripcion = "Gorro de invierno con auriculares Bluetooth integrados para escuchar música y mantenerte caliente.",
                precio = 45.00,
                talla = "Única",
                stock = 15,
                categoria = "Novedades",
                imagenUrl = "https://example.com/gorro-auriculares.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Reloj GPS",
                descripcion = "Reloj inteligente para niños con GPS para rastrear su ubicación en todo momento.",
                precio = 75.00,
                talla = "Única",
                stock = 10,
                categoria = "Novedades",
                imagenUrl = "https://example.com/reloj-gps.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Mochila con proyector",
                descripcion = "Mochila divertida que incluye un proyector para mostrar imágenes en cualquier superficie.",
                precio = 80.00,
                talla = "Única",
                stock = 12,
                categoria = "Novedades",
                imagenUrl = "https://example.com/mochila-proyector.jpg", // URL de ejemplo
                genero = "Niño"
            ),

// NIÑA

            Producto(
                nombre = "Zapatillas con glitter y luces LED",
                descripcion = "Zapatillas con brillo y luces LED, ideales para un look brillante y moderno.",
                precio = 55.00,
                talla = "30",
                stock = 20,
                categoria = "Novedades",
                imagenUrl = "https://example.com/zapatillas-glitter.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Vestido mágico con tinta térmica",
                descripcion = "Vestido que cambia de color al contacto con el calor, creando un efecto visual mágico.",
                precio = 60.00,
                talla = "6",
                stock = 18,
                categoria = "Novedades",
                imagenUrl = "https://example.com/vestido-magico.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Mochila con espejo y cepillo incluidos",
                descripcion = "Mochila que incluye un espejo y un cepillo, perfecta para las niñas que les gusta verse y organizarse.",
                precio = 40.00,
                talla = "Única",
                stock = 25,
                categoria = "Novedades",
                imagenUrl = "https://example.com/mochila-espejo.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Diadema con altavoz bluetooth",
                descripcion = "Diadema con altavoces Bluetooth integrados, para que las niñas puedan escuchar su música favorita.",
                precio = 30.00,
                talla = "Única",
                stock = 15,
                categoria = "Novedades",
                imagenUrl = "https://example.com/diadema-bluetooth.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Reloj de princesas con GPS",
                descripcion = "Reloj con diseño de princesas y tecnología GPS, perfecto para mantener rastreo en tiempo real.",
                precio = 70.00,
                talla = "Única",
                stock = 10,
                categoria = "Novedades",
                imagenUrl = "https://example.com/reloj-princesa-gps.jpg", // URL de ejemplo
                genero = "Niña"
            ),

            // HOMBRE

            Producto(
                nombre = "Gorro beanie",
                descripcion = "Gorro de lana tipo beanie, ideal para mantenerte cálido durante el invierno.",
                precio = 25.00,
                talla = "Única",
                stock = 30,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/gorro-beanie.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Gorra plana Adidas",
                descripcion = "Gorra plana con el logo de Adidas, perfecta para un look urbano y deportivo.",
                precio = 30.00,
                talla = "Única",
                stock = 20,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/gorra-adidas.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Mochila urbana Nike",
                descripcion = "Mochila con compartimentos organizados, ideal para actividades diarias y deportivas.",
                precio = 70.00,
                talla = "Única",
                stock = 15,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/mochila-urbana-nike.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Riñonera cruzada",
                descripcion = "Riñonera deportiva de estilo cruzado, perfecta para llevar lo esencial de forma cómoda.",
                precio = 40.00,
                talla = "Única",
                stock = 25,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/rinonera-cruzada.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Pulsera deportiva",
                descripcion = "Pulsera de silicona con detalles deportivos, ideal para el entrenamiento o actividades al aire libre.",
                precio = 15.00,
                talla = "Única",
                stock = 50,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/pulsera-deportiva.jpg", // URL de ejemplo
                genero = "Hombre"
            ),

// MUJER

            Producto(
                nombre = "Bolso deportivo",
                descripcion = "Bolso deportivo con diseño funcional y elegante, ideal para el gimnasio o actividades diarias.",
                precio = 65.00,
                talla = "Única",
                stock = 18,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/bolso-deportivo.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Canguro de moda",
                descripcion = "Riñonera de estilo moderno y práctico, perfecta para tener las manos libres mientras te mueves.",
                precio = 40.00,
                talla = "Única",
                stock = 20,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/canguro-de-moda.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Gafas de sol oversized",
                descripcion = "Gafas de sol con un diseño oversized, ideal para un look elegante y protección UV.",
                precio = 50.00,
                talla = "Única",
                stock = 25,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/gafas-oversized.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Bandana o pañoleta",
                descripcion = "Pañoleta de tela con diseño versátil que puede usarse como diadema o bufanda.",
                precio = 12.00,
                talla = "Única",
                stock = 40,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/bandana.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Scrunchies",
                descripcion = "Lazos de tela elástica para el cabello, suaves y cómodos, ideales para mantener el estilo.",
                precio = 10.00,
                talla = "Única",
                stock = 60,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/scrunchies.jpg", // URL de ejemplo
                genero = "Mujer"
            ),

// NIÑO

            Producto(
                nombre = "Gorra Pokémon",
                descripcion = "Gorra de estilo casual con diseño de Pokémon, perfecta para los fanáticos de la serie.",
                precio = 18.00,
                talla = "Única",
                stock = 25,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/gorra-pokemon.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Mochila escolar",
                descripcion = "Mochila infantil con compartimentos y diseño de personajes, ideal para la escuela.",
                precio = 40.00,
                talla = "Única",
                stock = 30,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/mochila-escolar.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Reloj Spiderman",
                descripcion = "Reloj infantil con diseño de Spiderman, resistente al agua y con alarma.",
                precio = 25.00,
                talla = "Única",
                stock = 15,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/reloj-spiderman.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Riñonera deportiva",
                descripcion = "Riñonera compacta y ligera, perfecta para niños activos que necesitan llevar lo esencial.",
                precio = 20.00,
                talla = "Única",
                stock = 40,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/rinonera-deportiva.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Lentes de sol con diseño",
                descripcion = "Lentes de sol coloridos y divertidos, ideales para proteger los ojos mientras juegan al aire libre.",
                precio = 15.00,
                talla = "Única",
                stock = 35,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/lentes-sol-nino.jpg", // URL de ejemplo
                genero = "Niño"
            ),

// NIÑA

            Producto(
                nombre = "Diadema con orejas",
                descripcion = "Diadema suave con orejas de animalito, perfecta para un look divertido y tierno.",
                precio = 12.00,
                talla = "Única",
                stock = 50,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/diadema-orejas.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Gorro con glitter",
                descripcion = "Gorro de invierno con detalles de glitter, ideal para mantener la cabeza cálida y brillante.",
                precio = 18.00,
                talla = "Única",
                stock = 25,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/gorro-glitter.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Bolso de lentejuelas",
                descripcion = "Bolso pequeño con lentejuelas reversibles, ideal para ocasiones especiales y el día a día.",
                precio = 22.00,
                talla = "Única",
                stock = 30,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/bolso-lentejuelas.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Mochila de unicornio",
                descripcion = "Mochila pequeña con un diseño de unicornio, perfecta para las niñas que aman los animales mágicos.",
                precio = 28.00,
                talla = "Única",
                stock = 20,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/mochila-unicornio.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Lentes de sol con forma de corazón",
                descripcion = "Lentes de sol con diseño de corazón, perfectos para un look veraniego y divertido.",
                precio = 15.00,
                talla = "Única",
                stock = 40,
                categoria = "Accesorios",
                imagenUrl = "https://example.com/lentes-corazon.jpg", // URL de ejemplo
                genero = "Niña"
            ),

            // HOMBRE

            Producto(
                nombre = "Camiseta básica algodón",
                descripcion = "Camiseta de algodón de corte clásico, perfecta para el uso diario y en múltiples colores.",
                precio = 19.99,
                talla = "M",
                stock = 30,
                categoria = "Ropa",
                imagenUrl = "https://example.com/camiseta-basica-hombre.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Buzo Nike Sportswear",
                descripcion = "Buzo con capucha y logo de Nike, ideal para el gimnasio o un look casual.",
                precio = 60.00,
                talla = "L",
                stock = 20,
                categoria = "Ropa",
                imagenUrl = "https://example.com/buzo-nike.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Pantalón jogger Adidas",
                descripcion = "Pantalón jogger cómodo y ligero con el logo de Adidas, ideal para entrenar o el día a día.",
                precio = 55.00,
                talla = "L",
                stock = 15,
                categoria = "Ropa",
                imagenUrl = "https://example.com/pantalon-jogger-adidas.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Camisa de jean",
                descripcion = "Camisa de mezclilla con corte moderno, perfecta para ocasiones casuales y de estilo.",
                precio = 40.00,
                talla = "M",
                stock = 25,
                categoria = "Ropa",
                imagenUrl = "https://example.com/camisa-jean-hombre.jpg", // URL de ejemplo
                genero = "Hombre"
            ),
            Producto(
                nombre = "Chaqueta cortaviento Puma",
                descripcion = "Chaqueta ligera y funcional, ideal para actividades al aire libre y con protección contra el viento.",
                precio = 70.00,
                talla = "M",
                stock = 10,
                categoria = "Ropa",
                imagenUrl = "https://example.com/chaqueta-cortaviento-puma.jpg", // URL de ejemplo
                genero = "Hombre"
            ),

// MUJER

            Producto(
                nombre = "Leggings deportivos",
                descripcion = "Leggings cómodos y elásticos, ideales para el gimnasio o actividades deportivas.",
                precio = 35.00,
                talla = "S",
                stock = 30,
                categoria = "Ropa",
                imagenUrl = "https://example.com/leggings-deportivos.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Camiseta crop top",
                descripcion = "Camiseta corta de algodón, perfecta para días cálidos o como parte de un look casual.",
                precio = 20.00,
                talla = "M",
                stock = 25,
                categoria = "Ropa",
                imagenUrl = "https://example.com/camiseta-crop-top.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Hoodie oversize",
                descripcion = "Sudadera oversize de felpa, perfecta para un look relajado o para descansar en casa.",
                precio = 55.00,
                talla = "L",
                stock = 15,
                categoria = "Ropa",
                imagenUrl = "https://example.com/hoodie-oversize.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Falda deportiva",
                descripcion = "Falda deportiva cómoda y con estilo, ideal para hacer ejercicio o un look casual.",
                precio = 40.00,
                talla = "M",
                stock = 20,
                categoria = "Ropa",
                imagenUrl = "https://example.com/falda-deportiva.jpg", // URL de ejemplo
                genero = "Mujer"
            ),
            Producto(
                nombre = "Biker shorts",
                descripcion = "Shorts ajustados y elásticos, perfectos para el gimnasio o como parte de un look moderno.",
                precio = 30.00,
                talla = "S",
                stock = 25,
                categoria = "Ropa",
                imagenUrl = "https://example.com/biker-shorts.jpg", // URL de ejemplo
                genero = "Mujer"
            ),

// NIÑO

            Producto(
                nombre = "Camiseta Marvel",
                descripcion = "Camiseta con estampado de los superhéroes de Marvel, ideal para los fanáticos de los cómics.",
                precio = 18.00,
                talla = "8",
                stock = 30,
                categoria = "Ropa",
                imagenUrl = "https://example.com/camiseta-marvel.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Jogger con bolsillos",
                descripcion = "Pantalón jogger con bolsillos laterales, cómodo y funcional para actividades diarias.",
                precio = 25.00,
                talla = "7",
                stock = 40,
                categoria = "Ropa",
                imagenUrl = "https://example.com/jogger-nino.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Buzo con capucha",
                descripcion = "Buzo con capucha y detalles deportivos, ideal para mantener a los niños abrigados.",
                precio = 30.00,
                talla = "8",
                stock = 20,
                categoria = "Ropa",
                imagenUrl = "https://example.com/buzo-capucha-nino.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Polo a rayas",
                descripcion = "Polo de algodón con rayas coloridas, ideal para un look cómodo y fresco.",
                precio = 22.00,
                talla = "9",
                stock = 25,
                categoria = "Ropa",
                imagenUrl = "https://example.com/polo-rayas-nino.jpg", // URL de ejemplo
                genero = "Niño"
            ),
            Producto(
                nombre = "Chaqueta impermeable",
                descripcion = "Chaqueta ligera y resistente al agua, perfecta para días lluviosos.",
                precio = 35.00,
                talla = "M",
                stock = 18,
                categoria = "Ropa",
                imagenUrl = "https://example.com/chaqueta-impermeable-nino.jpg", // URL de ejemplo
                genero = "Niño"
            ),

// NIÑA

            Producto(
                nombre = "Leggings con estampado",
                descripcion = "Leggings cómodos con diseños divertidos y modernos, ideales para un look casual.",
                precio = 20.00,
                talla = "7",
                stock = 30,
                categoria = "Ropa",
                imagenUrl = "https://example.com/leggings-estampado-nina.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Camiseta Frozen",
                descripcion = "Camiseta con estampado de Frozen, perfecta para las fanáticas de la película.",
                precio = 18.00,
                talla = "8",
                stock = 35,
                categoria = "Ropa",
                imagenUrl = "https://example.com/camiseta-frozen-nina.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Vestido casual con volantes",
                descripcion = "Vestido elegante con volantes, ideal para ocasiones especiales o el día a día.",
                precio = 30.00,
                talla = "6",
                stock = 20,
                categoria = "Ropa",
                imagenUrl = "https://example.com/vestido-volantes-nina.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Buzo con glitter",
                descripcion = "Buzo de felpa con detalles de glitter, perfecto para un look moderno y cómodo.",
                precio = 28.00,
                talla = "M",
                stock = 25,
                categoria = "Ropa",
                imagenUrl = "https://example.com/buzo-glitter-nina.jpg", // URL de ejemplo
                genero = "Niña"
            ),
            Producto(
                nombre = "Camiseta manga larga",
                descripcion = "Camiseta de manga larga con estampado de animales, ideal para el otoño.",
                precio = 22.00,
                talla = "S",
                stock = 30,
                categoria = "Ropa",
                imagenUrl = "https://www.vertbaudet.es/fstrz/r/s/media.vertbaudet.es/Pictures/vertbaudet/279251/pack-de-3-camisetas-de-manga-larga-para-nina.jpg?width=457",
                genero = "Niña"
            )


        )

        productosEjemplo.forEach { producto ->
            agregarProducto(
                producto = producto,
                onSuccess = { 
                    Log.d("ProductoRepositorio", "Producto ejemplo ${producto.nombre} agregado exitosamente")
                },
                onFailure = { exception ->
                    Log.e("ProductoRepositorio", "Error al agregar producto ejemplo ${producto.nombre}: ${exception.message}")
                }
            )
        }
    }
}

