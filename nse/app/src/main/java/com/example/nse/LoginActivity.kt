package com.example.nse
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var textoRegistro: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        textoRegistro = findViewById(R.id.textoRegistro)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.
            getInstance("https://chatsbd-162f2-default-rtdb.europe-west1.firebasedatabase.app/")
                .reference //cambiar por la base de datos propia


        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)

            }else{
                showSnackbar("Por favor, ingresa tu correo y contraseña.")

        }


        }

        textoRegistro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuth", "Login exitoso")
                    val user = auth.currentUser
                    if (user != null) {
                        val userId = user.uid
                        database.child("users").child(userId).get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                showSnackbar("Bienvenido ${user.email}")
                                val intent = Intent(this, MessagesActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                showSnackbar("Usuario no encontrado en la base de datos")
                            }
                        }
                    }
                } else {
                    Log.e("FirebaseAuth", "Error en el login: ${task.exception?.message}")
                    showSnackbar("Error en el login: ${task.exception?.message}")
                }
            }
    }



    private fun showSnackbar(message: String) {
        val rootView = findViewById<android.view.View>(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }
}
