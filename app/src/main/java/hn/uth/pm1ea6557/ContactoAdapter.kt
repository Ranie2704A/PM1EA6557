package hn.uth.pm1ea6557

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.LinearLayout

class ContactoAdapter(private val context: Context, private val contactos: List<Contacto>) : BaseAdapter() {

    override fun getCount(): Int = contactos.size

    override fun getItem(position: Int): Any = contactos[position]

    override fun getItemId(position: Int): Long = contactos[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false)

        val layoutItem = view.findViewById<LinearLayout>(R.id.layoutItem)
        val txtNombreTelefono = view.findViewById<TextView>(R.id.txtNombreTelefono)
        val viewPunto = view.findViewById<View>(R.id.viewPunto)

        val contacto = contactos[position]
        txtNombreTelefono.text = "${contacto.nombre} | ${contacto.telefono}"

        if (position % 2 == 0) {
            layoutItem.setBackgroundColor(Color.parseColor("#2196F3"))
            viewPunto.setBackgroundResource(android.R.drawable.presence_online)
        } else {
            layoutItem.setBackgroundColor(Color.parseColor("#64B5F6"))
            viewPunto.setBackgroundResource(android.R.drawable.presence_busy)
        }

        return view
    }
}