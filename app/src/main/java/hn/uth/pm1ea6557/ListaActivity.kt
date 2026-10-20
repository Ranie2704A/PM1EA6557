package hn.uth.pm1ea6557

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ListaActivity : AppCompatActivity() {

    private lateinit var listViewContactos: ListView
    private lateinit var txtBuscar: EditText
    private lateinit var btnAtras: Button
    private lateinit var btnCompartirContacto: Button
    private lateinit var btnVerImagen: Button
    private lateinit var btnEliminarContacto: Button
    private lateinit var btnActualizarContacto: Button

    private val listaContactos = ArrayList<Contacto>()
    private val listaFiltrada = ArrayList<Contacto>()
    private lateinit var adapter: ContactoAdapter

    private var contactoSeleccionado: Contacto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)

        btnAtras = findViewById(R.id.btnAtras)
        txtBuscar = findViewById(R.id.txtBuscar)
        listViewContactos = findViewById(R.id.listViewContactos)
        btnCompartirContacto = findViewById(R.id.btnCompartirContacto)
        btnVerImagen = findViewById(R.id.btnVerImagen)
        btnEliminarContacto = findViewById(R.id.btnEliminarContacto)
        btnActualizarContacto = findViewById(R.id.btnActualizarContacto)

        adapter = ContactoAdapter(this, listaFiltrada)
        listViewContactos.adapter = adapter

        btnAtras.setOnClickListener { finish() }

        cargarContactos()

        listViewContactos.setOnItemClickListener { _, _, position, _ ->
            contactoSeleccionado = listaFiltrada[position]
            mostrarDialogoLlamar(contactoSeleccionado!!)
        }

        txtBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarContactos(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnCompartirContacto.setOnClickListener {
            val c = contactoSeleccionado
            if (c != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Contacto: ${c.nombre} - Teléfono: ${c.telefono}")
                }
                startActivity(Intent.createChooser(intent, "Compartir vía"))
            } else {
                Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show()
            }
        }

        btnEliminarContacto.setOnClickListener {
            val c = contactoSeleccionado
            if (c != null) {
                AlertDialog.Builder(this)
                    .setTitle("Eliminar Contacto")
                    .setMessage("¿Desea eliminar a ${c.nombre}?")
                    .setPositiveButton("Sí") { _, _ -> eliminarDeBD(c.id) }
                    .setNegativeButton("No", null)
                    .show()
            } else {
                Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show()
            }
        }

        btnVerImagen.setOnClickListener {
            val c = contactoSeleccionado
            if (c != null) {
                val imageView = ImageView(this).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        500
                    )
                    scaleType = ImageView.ScaleType.FIT_CENTER

                    if (c.imagen.isNotEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(c.imagen, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            setImageResource(android.R.drawable.ic_menu_camera)
                        }
                    } else {
                        setImageResource(android.R.drawable.ic_menu_camera)
                    }
                }

                AlertDialog.Builder(this)
                    .setTitle("Imagen de ${c.nombre}")
                    .setMessage("Nota: ${c.nota}")
                    .setView(imageView)
                    .setPositiveButton("Cerrar", null)
                    .show()
            } else {
                Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show()
            }
        }

        btnActualizarContacto.setOnClickListener {
            val c = contactoSeleccionado
            if (c != null) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("MODO_EDICION", true)
                    putExtra("ID", c.id)
                    putExtra("PAIS", c.pais)
                    putExtra("NOMBRE", c.nombre)
                    putExtra("TELEFONO", c.telefono)
                    putExtra("NOTA", c.nota)
                    putExtra("IMAGEN", c.imagen)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Seleccione un contacto de la lista para actualizar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarContactos()
    }

    private fun cargarContactos() {
        listaContactos.clear()
        val dbHelper = DbHelper(this)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM contactos", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val pais = cursor.getString(cursor.getColumnIndexOrThrow("pais"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono"))
                val nota = cursor.getString(cursor.getColumnIndexOrThrow("nota"))
                val imagen = cursor.getString(cursor.getColumnIndexOrThrow("imagen")) ?: ""

                listaContactos.add(Contacto(id, pais, nombre, telefono, nota, imagen))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        filtrarContactos(txtBuscar.text.toString())
    }

    private fun filtrarContactos(texto: String) {
        listaFiltrada.clear()
        for (c in listaContactos) {
            if (c.nombre.contains(texto, ignoreCase = true) || c.telefono.contains(texto)) {
                listaFiltrada.add(c)
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun mostrarDialogoLlamar(contacto: Contacto) {
        AlertDialog.Builder(this)
            .setTitle("Acción")
            .setMessage("¿Desea llamar a ${contacto.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                // Solo extraemos los números del país (ej. "Honduras (504)" a "504")
                val codigoPais = contacto.pais.filter { it.isDigit() }.ifEmpty { "504" }
                val telefonoCompleto = "+$codigoPais ${contacto.telefono}"

                // Navegamos a la Pantalla 3 que te tocó hacer
                val intent = Intent(this, LlamarActivity::class.java).apply {
                    putExtra("NOMBRE", contacto.nombre)
                    putExtra("TELEFONO", telefonoCompleto)
                }
                startActivity(intent)
            }
            // Si elige "No", el diálogo se cierra pero el contacto ya está asignado a 'contactoSeleccionado'
            .setNegativeButton("No", null)
            .show()
    }

    private fun eliminarDeBD(id: Long) {
        val dbHelper = DbHelper(this)
        val db = dbHelper.writableDatabase
        val res = db.delete("contactos", "id = ?", arrayOf(id.toString()))
        db.close()
        if (res > 0) {
            Toast.makeText(this, "Eliminado con éxito", Toast.LENGTH_SHORT).show()
            contactoSeleccionado = null
            cargarContactos()
        }
    }
}