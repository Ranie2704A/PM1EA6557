package hn.uth.pm1ea6557

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private var imagenBase64: String = ""
    private lateinit var imageViewFoto: ImageView
    private var idContactoActualizar: Long = -1L

    private val tomarFotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            imageViewFoto.setImageBitmap(bitmap)
            imagenBase64 = convertirBitmapABase64(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageViewFoto = findViewById(R.id.imageViewFoto)
        val btnAbrirCamara = findViewById<ImageButton>(R.id.btnAbrirCamara)
        val spinnerPais = findViewById<Spinner>(R.id.spinnerPais)
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        val editTextNota = findViewById<EditText>(R.id.editTextNota)
        val btnSalvarContactos = findViewById<Button>(R.id.btnSalvarContactos)
        val btnVerLista = findViewById<Button>(R.id.btnContactosSalvados)

        ArrayAdapter.createFromResource(
            this,
            R.array.paises_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPais.adapter = adapter
        }

        btnAbrirCamara.setOnClickListener {
            tomarFotoLauncher.launch(null)
        }

        val modoEdicion = intent.getBooleanExtra("MODO_EDICION", false)
        if (modoEdicion) {
            idContactoActualizar = intent.getLongExtra("ID", -1L)
            val paisRecibido = intent.getStringExtra("PAIS")
            val nombreRecibido = intent.getStringExtra("NOMBRE")
            val telefonoRecibido = intent.getStringExtra("TELEFONO")
            val notaRecibida = intent.getStringExtra("NOTA")

            editTextName.setText(nombreRecibido)
            editTextPhone.setText(telefonoRecibido)
            editTextNota.setText(notaRecibida)
            btnSalvarContactos.text = "Actualizar Registro"

            val adapter = spinnerPais.adapter as? ArrayAdapter<String>
            if (adapter != null && paisRecibido != null) {
                val position = adapter.getPosition(paisRecibido)
                if (position >= 0) spinnerPais.setSelection(position)
            }
        }

        btnSalvarContactos.setOnClickListener {
            val pais = spinnerPais.selectedItem?.toString() ?: ""
            val nombre = editTextName.text.toString().trim()
            val telefono = editTextPhone.text.toString().trim()
            val nota = editTextNota.text.toString().trim()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Alerta: Debe escribir un nombre", Toast.LENGTH_SHORT).show()
                editTextName.error = "Campo obligatorio"
                return@setOnClickListener
            }

            val regexNombre = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$".toRegex()
            if (!nombre.matches(regexNombre)) {
                Toast.makeText(this, "Alerta: El nombre solo debe contener letras", Toast.LENGTH_SHORT).show()
                editTextName.error = "Solo se permiten letras"
                return@setOnClickListener
            }

            if (telefono.isEmpty()) {
                Toast.makeText(this, "Alerta: Debe escribir un teléfono", Toast.LENGTH_SHORT).show()
                editTextPhone.error = "Campo obligatorio"
                return@setOnClickListener
            }

            val regexTelefono = "^[0-9]{8}$".toRegex()
            if (!telefono.matches(regexTelefono)) {
                Toast.makeText(this, "Alerta: Debe escribir un teléfono de 8 dígitos", Toast.LENGTH_LONG).show()
                editTextPhone.error = "Debe tener exactamente 8 dígitos"
                return@setOnClickListener
            }

            if (nota.isEmpty()) {
                Toast.makeText(this, "Alerta: Debe escribir una nota", Toast.LENGTH_SHORT).show()
                editTextNota.error = "Campo obligatorio"
                return@setOnClickListener
            }

            val dbHelper = DbHelper(this)
            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put("pais", pais)
                put("nombre", nombre)
                put("telefono", telefono)
                put("nota", nota)
                if (imagenBase64.isNotEmpty()) {
                    put("imagen", imagenBase64)
                }
            }

            if (idContactoActualizar != -1L) {
                val filasAfectadas = db.update("contactos", values, "id = ?", arrayOf(idContactoActualizar.toString()))
                db.close()

                if (filasAfectadas > 0) {
                    Toast.makeText(this, "¡Contacto actualizado con éxito!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar el contacto", Toast.LENGTH_SHORT).show()
                }
            } else {
                val resultado = db.insert("contactos", null, values)
                db.close()

                if (resultado != -1L) {
                    Toast.makeText(this, "¡Contacto guardado con éxito!", Toast.LENGTH_SHORT).show()
                    editTextName.text.clear()
                    editTextPhone.text.clear()
                    editTextNota.text.clear()
                    spinnerPais.setSelection(0)
                    imageViewFoto.setImageResource(0)
                    imagenBase64 = ""
                } else {
                    Toast.makeText(this, "Error al guardar el contacto", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnVerLista.setOnClickListener {
            val intent = Intent(this, ListaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun convertirBitmapABase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}