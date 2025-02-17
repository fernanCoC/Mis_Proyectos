package com.example.nse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.
        getInstance("https://chatsbd-162f2-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference // igual cambiarla por la base de datos propia

        val corrreo = findViewById<EditText>(R.id.etCorreo)
        val pass = findViewById<EditText>(R.id.etPass)
        val textoLogin = findViewById<TextView>(R.id.tvLogin)
        val btnRegistrar = findViewById<TextView>(R.id.btnRegistrar)

        btnRegistrar.setOnClickListener {
            val email = corrreo.text.toString()
            val password = pass.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: ""


                        val userMap = mapOf(
                            "email" to email,
                            "userId" to userId
                        )

                        database.child("users").child(userId).setValue(userMap)
                            .addOnSuccessListener {
                                Log.d("FirebaseAuth", "Iniciando activity...")
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MessagesActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al guardar datos: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        textoLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
