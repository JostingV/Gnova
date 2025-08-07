package com.proyecto.tienda.gnova.ui.activities.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.data.repositories.ProductoRepositorio

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
        
        // Botón temporal para inicializar productos de ejemplo
        findViewById<Button>(R.id.btnInicializarEjemplos)?.setOnClickListener {
            ProductoRepositorio.inicializarProductosEjemplo()
            Toast.makeText(this, "Inicializando productos de ejemplo...", Toast.LENGTH_LONG).show()
        }
    }

}
