package com.proyecto.tienda.gnova.data.models

data class ItemCarrito(
    val producto: Producto,
    var cantidad: Int = 1
)

object Carrito {
    private val itemsEnCarrito = mutableMapOf<String, ItemCarrito>()

    fun agregarProducto(producto: Producto) {
        val productoId = producto.id.ifEmpty { producto.nombre } // Usar ID o nombre como clave única
        
        if (itemsEnCarrito.containsKey(productoId)) {
            // Si ya existe, incrementar cantidad
            itemsEnCarrito[productoId]?.cantidad = (itemsEnCarrito[productoId]?.cantidad ?: 0) + 1
        } else {
            // Si no existe, agregar nuevo item
            itemsEnCarrito[productoId] = ItemCarrito(producto, 1)
        }
    }

    fun obtenerItems(): List<ItemCarrito> {
        return itemsEnCarrito.values.toList()
    }

    fun obtenerProductos(): List<Producto> {
        // Mantener compatibilidad con código existente
        val productos = mutableListOf<Producto>()
        itemsEnCarrito.values.forEach { item ->
            repeat(item.cantidad) {
                productos.add(item.producto)
            }
        }
        return productos
    }

    fun limpiarCarrito() {
        itemsEnCarrito.clear()
    }

    fun eliminarProducto(producto: Producto): Boolean {
        val productoId = producto.id.ifEmpty { producto.nombre }
        
        if (itemsEnCarrito.containsKey(productoId)) {
            val item = itemsEnCarrito[productoId]!!
            if (item.cantidad > 1) {
                // Decrementar cantidad
                item.cantidad -= 1
                return true
            } else {
                // Eliminar completamente si cantidad es 1
                itemsEnCarrito.remove(productoId)
                return true
            }
        }
        return false
    }

    fun eliminarItemCompleto(producto: Producto): Boolean {
        val productoId = producto.id.ifEmpty { producto.nombre }
        return itemsEnCarrito.remove(productoId) != null
    }

    fun actualizarCantidad(producto: Producto, nuevaCantidad: Int) {
        val productoId = producto.id.ifEmpty { producto.nombre }
        
        if (nuevaCantidad <= 0) {
            itemsEnCarrito.remove(productoId)
        } else {
            itemsEnCarrito[productoId]?.cantidad = nuevaCantidad
        }
    }

    fun obtenerCantidadTotal(): Int {
        return itemsEnCarrito.values.sumOf { it.cantidad }
    }

    fun obtenerTotal(): Double {
        return itemsEnCarrito.values.sumOf { it.producto.precio * it.cantidad }
    }
}
