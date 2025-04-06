package lab.jhrodriguezi.sqlite.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "CompanyDirectory.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_COMPANIES = "companies"
        
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_WEBSITE = "website"
        private const val KEY_PHONE = "phone"
        private const val KEY_EMAIL = "email"
        private const val KEY_PRODUCTS = "products"
        private const val KEY_CLASSIFICATION = "classification"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_COMPANIES (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_NAME TEXT NOT NULL,
                $KEY_WEBSITE TEXT,
                $KEY_PHONE TEXT,
                $KEY_EMAIL TEXT,
                $KEY_PRODUCTS TEXT,
                $KEY_CLASSIFICATION TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPANIES")
        onCreate(db)
    }

    fun insertCompany(company: Company): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, company.name)
            put(KEY_WEBSITE, company.website)
            put(KEY_PHONE, company.phone)
            put(KEY_EMAIL, company.email)
            put(KEY_PRODUCTS, company.products)
            put(KEY_CLASSIFICATION, company.classification)
        }
        return db.insert(TABLE_COMPANIES, null, values)
    }

    fun updateCompany(company: Company): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, company.name)
            put(KEY_WEBSITE, company.website)
            put(KEY_PHONE, company.phone)
            put(KEY_EMAIL, company.email)
            put(KEY_PRODUCTS, company.products)
            put(KEY_CLASSIFICATION, company.classification)
        }
        return db.update(TABLE_COMPANIES, values, "$KEY_ID = ?", arrayOf(company.id.toString()))
    }

    fun deleteCompany(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_COMPANIES, "$KEY_ID = ?", arrayOf(id.toString()))
    }

    fun getCompany(id: Int): Company? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_COMPANIES,
            null,
            "$KEY_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Company(
                    id = it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(KEY_NAME)),
                    website = it.getString(it.getColumnIndexOrThrow(KEY_WEBSITE)),
                    phone = it.getString(it.getColumnIndexOrThrow(KEY_PHONE)),
                    email = it.getString(it.getColumnIndexOrThrow(KEY_EMAIL)),
                    products = it.getString(it.getColumnIndexOrThrow(KEY_PRODUCTS)),
                    classification = it.getString(it.getColumnIndexOrThrow(KEY_CLASSIFICATION))
                )
            } else null
        }
    }

    fun searchCompanies(nameQuery: String = "", classification: String? = null): List<Company> {
        val db = this.readableDatabase
        val companies = mutableListOf<Company>()
        
        var selection = ""
        val selectionArgs = mutableListOf<String>()
        
        if (nameQuery.isNotEmpty()) {
            selection += "$KEY_NAME LIKE ?"
            selectionArgs.add("%$nameQuery%")
        }
        
        if (classification != null) {
            if (selection.isNotEmpty()) selection += " AND "
            selection += "$KEY_CLASSIFICATION = ?"
            selectionArgs.add(classification)
        }

        val cursor = db.query(
            TABLE_COMPANIES,
            null,
            if (selection.isEmpty()) null else selection,
            if (selectionArgs.isEmpty()) null else selectionArgs.toTypedArray(),
            null,
            null,
            "$KEY_NAME ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                companies.add(
                    Company(
                        id = it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(KEY_NAME)),
                        website = it.getString(it.getColumnIndexOrThrow(KEY_WEBSITE)),
                        phone = it.getString(it.getColumnIndexOrThrow(KEY_PHONE)),
                        email = it.getString(it.getColumnIndexOrThrow(KEY_EMAIL)),
                        products = it.getString(it.getColumnIndexOrThrow(KEY_PRODUCTS)),
                        classification = it.getString(it.getColumnIndexOrThrow(KEY_CLASSIFICATION))
                    )
                )
            }
        }
        return companies
    }
}