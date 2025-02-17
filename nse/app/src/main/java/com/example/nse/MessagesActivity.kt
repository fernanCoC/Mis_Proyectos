package com.example.nse

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MessagesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var mensajeContenedor: TextView
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mensajes_activity)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://chatsbd-162f2-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference.child("messages")

        val etMensaje = findViewById<EditText>(R.id.etMensaje)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        mensajeContenedor = findViewById(R.id.mensajeContenedor)

        userId = auth.currentUser?.uid ?: "Desconocido"

        btnEnviar.setOnClickListener {
            val mensaje = etMensaje.text.toString().trim()

            if (mensaje.isNotEmpty()) {
                val userId = auth.currentUser?.email ?: "Desconocido"

                val mensajeData = mapOf(
                    "userId" to userId,
                    "mensaje" to mensaje
                )

                FirebaseDatabase.getInstance().reference.child("messages").push().setValue(mensajeData)
                    .addOnSuccessListener {
                        mensajeContenedor.append("\nTú: $mensaje")
                        etMensaje.text.clear()
                    }
                    .addOnFailureListener {
                        mensajeContenedor.append("\n❌ Error al enviar mensaje")
                    }
            }
        }


        escucharMensajes()
    }

    /*private fun enviarMensaje(mensaje: String) {
        val mensajeData = mapOf(
            "userId" to userId,
            "mensaje" to mensaje
        )
        database.push().setValue(mensajeData)
    }*/

    private fun escucharMensajes() {
        val mensajesRef = FirebaseDatabase.getInstance().reference.child("messages")

        mensajesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val userIdMensaje = snapshot.child("userId").getValue(String::class.java) ?: "Desconocido"
                val textoMensaje = snapshot.child("mensaje").getValue(String::class.java) ?: ""

                runOnUiThread {
                    mensajeContenedor.append("\n[$userIdMensaje]: $textoMensaje")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                runOnUiThread {
                    mensajeContenedor.append("\n❌ Error en Firebase: ${error.message}")
                }
            }
        })
    }

}
