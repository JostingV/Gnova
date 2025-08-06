package com.proyecto.tienda.gnova

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class InicioAdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio_admin)
        findViewById<Button>(R.id.btnVerProductos).setOnClickListener {
            startActivity(Intent(this, ListaProductosAdminActivity::class.java))
        }

        findViewById<Button>(R.id.btnAgregarProducto).setOnClickListener {
            startActivity(Intent(this, AgregarProductoActivity::class.java))
        }

        findViewById<Button>(R.id.btnConfiguracion).setOnClickListener {
            startActivity(Intent(this, ConfiguracionAdminActivity::class.java))
        }
    }

}
