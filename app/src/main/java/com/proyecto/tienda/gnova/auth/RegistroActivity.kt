package com.proyecto.tienda.gnova.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.FirebaseManager
import com.proyecto.tienda.gnova.ui.activities.client.InicioClienteActivity

class RegistroActivity : AppCompatActivity() {
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etClave: EditText
    private lateinit var btnRegistrarse: Button
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Firebase PRIMERO
        FirebaseManager.initialize(this)
        
        setContentView(R.layout.activity_registro)

        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etClave = findViewById(R.id.etClave)
        btnRegistrarse = findViewById(R.id.btnRegistrarse)
        btnLogin = findViewById(R.id.btnLogin)

        btnRegistrarse.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val clave = etClave.text.toString().trim()

            if (nombre.isEmpty() || correo.isEmpty() || clave.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar formato de email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Por favor ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar datos del cliente en Firestore
            val db = FirebaseManager.getFirestore()
            val usersRef = db.collection("users")
            val userData = hashMapOf(
                "nombre" to nombre,
                "email" to correo,
                "password" to clave,
                "role" to "client"
            )
            usersRef.document(correo).set(userData)
                .addOnSuccessListener {
                    val prefs = getSharedPreferences("gnova_prefs", MODE_PRIVATE)
                    prefs.edit().putString("nombre", nombre)
                        .putString("correo", correo)
                        .apply()
                    Toast.makeText(this, "Registro exitoso. ¡Bienvenido, $nombre!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, InicioClienteActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al registrar usuario en Firestore", Toast.LENGTH_SHORT).show()
                }
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
