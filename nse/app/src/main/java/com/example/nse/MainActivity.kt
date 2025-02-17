package com.example.nse
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.net.SocketException
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.*

class MainActivity : AppCompatActivity() {

    private lateinit var responseText: TextView
    private lateinit var btnConex: Button
    private lateinit var viewModel: ClientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        responseText = findViewById(R.id.responseText)
        btnConex = findViewById(R.id.btnConex)

        viewModel = ClientViewModel(this)

        btnConex.setOnClickListener {
            viewModel.sendMessage { serverResponse ->
                responseText.text = serverResponse
            }
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}

class ClientViewModel(private val context: Context) : ViewModel() {

    fun sendMessage(onResponseReceived: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            var cliente: SSLSocket? = null
            var flujoEntrada: DataInputStream? = null
            var flujoSalida: DataOutputStream? = null
            var socketCerrado = false

            try {
                val host = "192.168.1.35" //cambiar ip por la de tu pc
                val puerto = 5556

                val sslSocketFactory = createSSLSocketFactory(context)
                cliente = sslSocketFactory.createSocket(host, puerto) as SSLSocket

                cliente.soTimeout = 10000

                try {
                    cliente.startHandshake()
                    Log.d("SSLClient", "Handshake SSL completado correctamente")
                } catch (e: Exception) {
                    Log.e("SSLClient", "Error en el handshake SSL/TLS: ${e.message}", e)
                    onResponseReceived("Error en el handshake SSL/TLS: ${e.message}")
                    return@launch
                }

                flujoSalida = DataOutputStream(cliente.getOutputStream())
                flujoEntrada = DataInputStream(cliente.getInputStream())

                flujoSalida.writeUTF("Saludos al SERVIDOR DESDE EL CLIENTE")
                flujoSalida.flush()
                Log.d("SSLClient", "Mensaje enviado al servidor, esperando respuesta...")

                try {
                    val serverResponse = flujoEntrada.readUTF()
                    Log.d("SSLClient", "Respuesta recibida: $serverResponse")
                    onResponseReceived(serverResponse)

                } catch (e: Exception) {
                    Log.e("SSLClient", "Error al leer la respuesta: ${e.message}", e)
                    onResponseReceived("Error al leer la respuesta: ${e.message}")
                } finally {
                    if (!socketCerrado) {
                        try {
                            flujoEntrada?.close()
                            flujoSalida?.close()

                            if (!cliente.isClosed) {
                                cliente.close()
                                socketCerrado = true
                                Log.d("SSLClient", "Socket cerrado correctamente.")
                            }
                        } catch (e: SocketException) {
                        } catch (e: Exception) {
                            Log.e("SSLClient", "Error inesperado al cerrar el socket: ${e.message}", e)
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("SSLClient", "Error en la conexión: ${e.message}", e)
                onResponseReceived("Error en la conexión: ${e.message}")
            }
        }
    }

    private fun createSSLSocketFactory(context: Context): SSLSocketFactory {
        try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certInputStream: InputStream = context.resources.openRawResource(R.raw.servidor_publico)
            val certificado: Certificate = certificateFactory.generateCertificate(certInputStream)
            certInputStream.close()

            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry("servidor", certificado)

            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)

            val sslContext = SSLContext.getInstance("TLSv1.3")
            sslContext.init(null, trustManagerFactory.trustManagers, null)

            return sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException("❌ Error al cargar el certificado en Android: ${e.message}", e)
        }
    }
}
