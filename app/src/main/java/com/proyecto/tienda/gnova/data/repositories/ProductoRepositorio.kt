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
                imagenUrl = "", // Usará el logo como placeholder
                genero = "Hombre"
            ),
            Producto(
                nombre = "Camiseta Deportiva",
                descripcion = "Camiseta transpirable para entrenamientos",
                precio = 29.99,
                talla = "L",
                stock = 25,
                categoria = "Ropa",
                imagenUrl = "", // Usará el logo como placeholder
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
