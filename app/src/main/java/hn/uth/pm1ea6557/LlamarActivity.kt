package hn.uth.pm1ea6557

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LlamarActivity : AppCompatActivity() {

    private lateinit var tvNombreContacto: TextView
    private lateinit var tvTelefonoContacto: TextView
    private lateinit var btnAtras: Button
    private lateinit var btnLlamar: FloatingActionButton

    private var telefono: String = ""

    private val pedirPermisoLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                ejecutarLlamadaDirecta()
            } else {
                Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_llamar)

        tvNombreContacto = findViewById(R.id.tvNombreContacto)
        tvTelefonoContacto = findViewById(R.id.tvTelefonoContacto)
        btnAtras = findViewById(R.id.btnAtras)
        btnLlamar = findViewById(R.id.btnLlamar)

        val nombre = intent.getStringExtra("NOMBRE") ?: "Contacto"
        telefono = intent.getStringExtra("TELEFONO") ?: ""

        tvNombreContacto.text = nombre
        tvTelefonoContacto.text = telefono

        btnAtras.setOnClickListener {
            finish()
        }

        btnLlamar.setOnClickListener {
            verificarPermisoYLlamar()
        }
    }

    private fun verificarPermisoYLlamar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            ejecutarLlamadaDirecta()
        } else {
            pedirPermisoLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun ejecutarLlamadaDirecta() {
        val numeroLimpio = telefono.replace(" ", "").replace("-", "")

        val llamarIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$numeroLimpio")
        }

        try {
            startActivity(llamarIntent)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error de seguridad al intentar llamar", Toast.LENGTH_SHORT).show()
        }
    }
}