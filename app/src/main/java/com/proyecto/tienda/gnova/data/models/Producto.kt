package com.proyecto.tienda.gnova.data.models

import android.os.Parcel
import android.os.Parcelable

data class Producto(
    val id: String = "", // ID de Firestore, con valor por defecto
    val nombre: String = "", // Valor por defecto
    val descripcion: String = "", // Valor por defecto
    val precio: Double = 0.0, // Valor por defecto
    val talla: String = "", // Valor por defecto
    val stock: Int = 0, // Valor por defecto
    val categoria: String = "", // Valor por defecto
    val imagenUrl: String = "", // Valor por defecto
    val genero: String = "" // Valor por defecto
) : Parcelable { // Implementamos Parcelable en lugar de Serializable

    // Implementación de Parcelable
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nombre)
        parcel.writeString(descripcion)
        parcel.writeDouble(precio)
        parcel.writeString(talla)
        parcel.writeInt(stock)
        parcel.writeString(categoria)
        parcel.writeString(imagenUrl)
        parcel.writeString(genero)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Producto> {
        override fun createFromParcel(parcel: Parcel): Producto {
            return Producto(parcel)
        }

        override fun newArray(size: Int): Array<Producto?> {
            return arrayOfNulls(size)
        }
    }
}
