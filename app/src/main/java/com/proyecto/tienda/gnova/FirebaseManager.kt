package com.proyecto.tienda.gnova

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context

object FirebaseManager {
    private var db: FirebaseFirestore? = null
    
    fun initialize(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        db = FirebaseFirestore.getInstance()
    }
    
    fun getFirestore(): FirebaseFirestore {
        return db ?: throw IllegalStateException("Firebase no ha sido inicializado")
    }
}
