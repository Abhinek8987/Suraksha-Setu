package com.example.surakshasetu.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "suraksha_setu.db"
        private const val DATABASE_VERSION = 1

        // USERS TABLE
        const val TABLE_USERS = "users"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_PHONE = "phone"

        // SAFE CIRCLE TABLE
        const val TABLE_SAFE_CIRCLE = "safe_circle"
        const val COL_RELATION = "relation"

        // VOLUNTEERS TABLE
        const val TABLE_VOLUNTEERS = "volunteers"
        const val COL_VILLAGE = "village"
        const val COL_LAT = "latitude"
        const val COL_LON = "longitude"

        // ALERTS TABLE
        const val TABLE_ALERTS = "alerts"
        const val COL_TIMESTAMP = "timestamp"
        const val COL_AUDIO_PATH = "audioPath"

        // POLICE CONTACTS TABLE
        const val TABLE_POLICE = "police_contacts"
        const val COL_STATION = "stationName"
        const val COL_DISTRICT = "district"
        const val COL_POLICE_PHONE = "phone"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE $TABLE_USERS ("
                + "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_NAME TEXT,"
                + "$COL_PHONE TEXT)")
        db.execSQL(createUsersTable)

        val createSafeCircleTable = ("CREATE TABLE $TABLE_SAFE_CIRCLE ("
                + "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_NAME TEXT,"
                + "$COL_PHONE TEXT,"
                + "$COL_RELATION TEXT)")
        db.execSQL(createSafeCircleTable)

        val createVolunteersTable = ("CREATE TABLE $TABLE_VOLUNTEERS ("
                + "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_NAME TEXT,"
                + "$COL_PHONE TEXT,"
                + "$COL_VILLAGE TEXT,"
                + "$COL_LAT REAL,"
                + "$COL_LON REAL)")
        db.execSQL(createVolunteersTable)

        val createAlertsTable = ("CREATE TABLE $TABLE_ALERTS ("
                + "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_LAT REAL,"
                + "$COL_LON REAL,"
                + "$COL_TIMESTAMP TEXT,"
                + "$COL_AUDIO_PATH TEXT)")
        db.execSQL(createAlertsTable)

        val createPoliceTable = ("CREATE TABLE $TABLE_POLICE ("
                + "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_STATION TEXT,"
                + "$COL_POLICE_PHONE TEXT,"
                + "$COL_DISTRICT TEXT)")
        db.execSQL(createPoliceTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAFE_CIRCLE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VOLUNTEERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALERTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POLICE")
        onCreate(db)
    }

    // --- Safe Circle Methods ---
    fun addSafeCircleContact(name: String, emailOrPhone: String, relation: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_PHONE, emailOrPhone) // Storing email in the phone column for now
            put(COL_RELATION, relation)
        }
        val id = db.insert(TABLE_SAFE_CIRCLE, null, values)
        db.close()
        return id
    }

    fun getSafeCircleEmails(): List<String> {
        val emails = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COL_PHONE FROM $TABLE_SAFE_CIRCLE", null)
        if (cursor.moveToFirst()) {
            do {
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE))
                if (email.contains("@")) { // basic check if it's an email
                    emails.add(email)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return emails
    }

    fun getAllSafeCircleContacts(): List<SafeContact> {
        val contacts = mutableListOf<SafeContact>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAFE_CIRCLE", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE))
                contacts.add(SafeContact(id, name, email))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return contacts
    }

    fun deleteSafeCircleContact(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_SAFE_CIRCLE, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    // --- Alert Methods ---
    fun addAlert(lat: Double, lon: Double, timestamp: String, audioPath: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_LAT, lat)
            put(COL_LON, lon)
            put(COL_TIMESTAMP, timestamp)
            put(COL_AUDIO_PATH, audioPath)
        }
        val id = db.insert(TABLE_ALERTS, null, values)
        db.close()
        return id
    }
}

data class SafeContact(val id: Int, val name: String, val email: String) {
    override fun toString(): String {
        return "$name - $email"
    }
}
