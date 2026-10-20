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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LlamarActivity : AppCompatActivity() {

    private lateinit var tvNombreContacto: TextView
    private lateinit var tvTelefonoContacto: TextView
    private lateinit var btnAtras: Button
    private lateinit var btnLlamar: FloatingActionButton

    private var nombre: String = ""
    private var telefono: String = ""

    // 1. Registro del launcher para solicitar permisos en tiempo de ejecución
    private val pedirPermisoLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                realizarLlamadaDirecta()
            } else {
                Toast.makeText(this, "Permiso CALL_PHONE denegado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_llamar)

        tvNombreContacto = findViewById(R.id.tvNombreContacto)
        tvTelefonoContacto = findViewById(R.id.tvTelefonoContacto)
        btnAtras = findViewById(R.id.btnAtras)
        btnLlamar = findViewById(R.id.btnLlamar)

        // Obtener datos enviados desde la Pantalla 2 (Lista)
        nombre = intent.getStringExtra("NOMBRE") ?: "Contacto"
        telefono = intent.getStringExtra("TELEFONO") ?: ""

        tvNombreContacto.text = nombre
        tvTelefonoContacto.text = telefono

        // Evento botón Atrás
        btnAtras.setOnClickListener {
            finish()
        }

        // Evento botón Llamar
        btnLlamar.setOnClickListener {
            confirmarLlamadaDialog()
        }
    }

    // 2. Confirmar la llamada con un AlertDialog.Builder
    private fun confirmarLlamadaDialog() {
        AlertDialog.Builder(this)
            .setTitle("Acción")
            .setMessage("¿Desea llamar a $nombre?")
            .setPositiveButton("Sí") { dialog, _ ->
                dialog.dismiss()
                validarPermisoYLlamar()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    // 3. Validar si ya cuenta con el permiso o solicitarlo
    private fun validarPermisoYLlamar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            realizarLlamadaDirecta()
        } else {
            pedirPermisoLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    // 4. Ejecutar la llamada con Intent.ACTION_CALL
    private fun realizarLlamadaDirecta() {
        // Elimina espacios o guiones dejando el formato tel:+50499765645
        val numeroLimpio = telefono.replace(" ", "").replace("-", "")
        val llamarIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$numeroLimpio")
        }
        startActivity(llamarIntent)
    }
}