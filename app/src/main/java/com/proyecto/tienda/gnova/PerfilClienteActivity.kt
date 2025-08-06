package com.proyecto.tienda.gnova

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PerfilClienteActivity : AppCompatActivity() {
    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnEditarDatos: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_cliente)

        tvNombre = findViewById(R.id.tvNombreCliente)
        tvCorreo = findViewById(R.id.tvCorreoCliente)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        btnEditarDatos = findViewById(R.id.btnEditarDatos)

        // Obtener datos del cliente desde SharedPreferences
        val prefs = getSharedPreferences("gnova_prefs", MODE_PRIVATE)
        val nombre = prefs.getString("nombre", "Nombre no disponible")
        val correo = prefs.getString("correo", "Correo no disponible")

        tvNombre.text = "Nombre: $nombre"
        tvCorreo.text = "Correo: $correo"

        btnEditarDatos.setOnClickListener {
            Toast.makeText(this, getString(R.string.msg_editar_datos), Toast.LENGTH_SHORT).show()
        }

        btnCerrarSesion.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

}