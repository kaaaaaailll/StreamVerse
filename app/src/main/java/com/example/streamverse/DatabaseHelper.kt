package com.example.streamverse

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "streamverse.db"
        const val DATABASE_VERSION = 1

        const val TABLE_CONTENT = "content"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_DESCRIPTION = "description"
        const val COL_EPISODE = "episode"
        const val COL_RATING = "rating"
        const val COL_CATEGORY = "category"
        const val COL_STATUS = "status"
        const val COL_IS_ANIME = "is_anime"
        const val COL_IMAGE_URI = "image_uri"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_CONTENT (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_DESCRIPTION TEXT,
                $COL_EPISODE TEXT,
                $COL_RATING TEXT,
                $COL_CATEGORY TEXT,
                $COL_STATUS TEXT,
                $COL_IS_ANIME INTEGER,
                $COL_IMAGE_URI TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTENT")
        onCreate(db)
    }

    // INSERT
    fun insertContent(item: ContentItem): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, item.title)
            put(COL_DESCRIPTION, item.description)
            put(COL_EPISODE, item.episode)
            put(COL_RATING, item.rating)
            put(COL_CATEGORY, item.category)
            put(COL_STATUS, item.status)
            put(COL_IS_ANIME, if (item.isAnime) 1 else 0)
            put(COL_IMAGE_URI, item.imageUri)
        }
        val id = db.insert(TABLE_CONTENT, null, values)
        db.close()
        return id
    }

    // GET ALL ANIME
    fun getAllAnime(): MutableList<ContentItem> {
        return getContentByType(isAnime = true)
    }

    // GET ALL DRAMA
    fun getAllDrama(): MutableList<ContentItem> {
        return getContentByType(isAnime = false)
    }

    // GET BY TYPE
    private fun getContentByType(isAnime: Boolean): MutableList<ContentItem> {
        val list = mutableListOf<ContentItem>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CONTENT,
            null,
            "$COL_IS_ANIME = ?",
            arrayOf(if (isAnime) "1" else "0"),
            null, null,
            "$COL_ID DESC"
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    ContentItem(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)) ?: "",
                        episode = cursor.getString(cursor.getColumnIndexOrThrow(COL_EPISODE)) ?: "",
                        rating = cursor.getString(cursor.getColumnIndexOrThrow(COL_RATING)) ?: "5",
                        category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)) ?: "Love",
                        status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)) ?: "Ongoing",
                        isAnime = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ANIME)) == 1,
                        imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_URI))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // UPDATE
    fun updateContent(item: ContentItem): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, item.title)
            put(COL_DESCRIPTION, item.description)
            put(COL_EPISODE, item.episode)
            put(COL_RATING, item.rating)
            put(COL_CATEGORY, item.category)
            put(COL_STATUS, item.status)
            put(COL_IS_ANIME, if (item.isAnime) 1 else 0)
            put(COL_IMAGE_URI, item.imageUri)
        }
        val rows = db.update(TABLE_CONTENT, values, "$COL_ID = ?", arrayOf(item.id.toString()))
        db.close()
        return rows
    }

    // DELETE
    fun deleteContent(id: Long): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_CONTENT, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }
}