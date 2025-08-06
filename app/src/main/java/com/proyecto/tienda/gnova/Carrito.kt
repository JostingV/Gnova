package com.proyecto.tienda.gnova

object Carrito {
    private val productosEnCarrito = mutableListOf<Producto>()

    fun agregarProducto(producto: Producto) {
        productosEnCarrito.add(producto)
    }

    fun obtenerProductos(): List<Producto> {
        return productosEnCarrito.toList() // Retorna una copia para evitar modificaciones externas
    }

    fun limpiarCarrito() {
        productosEnCarrito.clear()
    }

    /**
     * Elimina una sola instancia de un producto del carrito.
     * Si hay múltiples instancias del mismo producto, solo elimina una.
     * @param producto El producto a eliminar.
     * @return true si el producto fue eliminado, false en caso contrario.
     */
    fun eliminarProducto(producto: Producto): Boolean {
        return productosEnCarrito.remove(producto)
    }
}
