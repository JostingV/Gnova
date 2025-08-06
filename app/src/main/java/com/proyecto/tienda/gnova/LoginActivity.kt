package com.proyecto.tienda.gnova

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

            // Lógica de autenticación (actualmente hardcodeada y guardado en SharedPreferences)
            if (email == "admin@gnova.com" && password == "admin123") {
                startActivity(Intent(this, InicioAdminActivity::class.java))
            } else {
                // Guardar correo y nombre para mostrarlo luego en perfil de cliente (simulación de login)
                val prefs = getSharedPreferences("gnova_prefs", MODE_PRIVATE)
                prefs.edit().putString("correo", email).apply()
                // Para el nombre, podríamos intentar recuperarlo si el correo ya existe,
                // o pedirlo en el registro y guardarlo. Aquí usaremos "Cliente Gnova" por defecto
                // si no se ha registrado un nombre antes.
                if (prefs.getString("nombre", null) == null) {
                    prefs.edit().putString("nombre", "Cliente Gnova").apply()
                }

                startActivity(Intent(this, InicioClienteActivity::class.java))
            }
            finish() // Cierra la actividad de Login
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }
}
