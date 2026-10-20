package hn.uth.pm1ea6557

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, "agenda.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE contactos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "pais TEXT, " +
                    "nombre TEXT, " +
                    "telefono TEXT, " +
                    "nota TEXT,"+
                    "imagen TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS contactos")
        onCreate(db)
    }
}