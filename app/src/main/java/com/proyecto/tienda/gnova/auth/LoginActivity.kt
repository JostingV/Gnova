package com.proyecto.tienda.gnova.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.proyecto.tienda.gnova.R
import com.proyecto.tienda.gnova.FirebaseManager
import com.proyecto.tienda.gnova.ui.activities.admin.InicioAdminActivity
import com.proyecto.tienda.gnova.ui.activities.client.InicioClienteActivity
import com.proyecto.tienda.gnova.data.repositories.ProductoRepositorio

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Firebase PRIMERO
        FirebaseManager.initialize(this)
        
        // Cargar productos de ejemplo automáticamente
        cargarProductosIniciales()
        
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar formato de email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Por favor ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseManager.getFirestore()
            val usersRef = db.collection("users")

            // Guardar admin si no existe
            val adminEmail = "admin@gnova.com"
            val adminPassword = "admin123"
            if (email == adminEmail && password == adminPassword) {
                usersRef.document(adminEmail).get().addOnSuccessListener { doc ->
                    if (!doc.exists()) {
                        val adminData = hashMapOf(
                            "email" to adminEmail,
                            "password" to adminPassword,
                            "role" to "admin"
                        )
                        usersRef.document(adminEmail).set(adminData)
                    }
                    // Login como admin
                    startActivity(Intent(this, InicioAdminActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error accediendo a Firestore", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            // Autenticación de usuario normal
            usersRef.document(email).get().addOnSuccessListener { doc ->
                if (doc.exists() && doc.getString("password") == password) {
                    val role = doc.getString("role") ?: "client"
                    if (role == "admin") {
                        startActivity(Intent(this, InicioAdminActivity::class.java))
                    } else {
                        val prefs = getSharedPreferences("gnova_prefs", MODE_PRIVATE)
                        prefs.edit().putString("correo", email).apply()
                        if (prefs.getString("nombre", null) == null) {
                            prefs.edit().putString("nombre", "Cliente Gnova").apply()
                        }
                        startActivity(Intent(this, InicioClienteActivity::class.java))
                    }
                    finish()
                } else {
                    Toast.makeText(this, "Credenciales incorrectas o usuario no registrado", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error accediendo a Firestore", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun cargarProductosIniciales() {
        // Verificar si ya se cargaron productos antes
        val prefs = getSharedPreferences("gnova_prefs", MODE_PRIVATE)
        val productosYaCargados = prefs.getBoolean("productos_cargados", false)
        
        if (!productosYaCargados) {
            ProductoRepositorio.inicializarProductosEjemplo()
            // Marcar como cargados para no duplicar
            prefs.edit().putBoolean("productos_cargados", true).apply()
            Toast.makeText(this, "Cargando productos iniciales...", Toast.LENGTH_SHORT).show()
        }
    }
}
