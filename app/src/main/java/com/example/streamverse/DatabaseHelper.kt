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
        const val DATABASE_VERSION = 2
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
        const val COL_IS_PINNED = "is_pinned"
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
                $COL_IMAGE_URI TEXT,
                $COL_IS_PINNED INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_CONTENT ADD COLUMN $COL_IS_PINNED INTEGER DEFAULT 0")
        }
    }

    fun insertContent(item: ContentItem): Long {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COL_TITLE, item.title)
                put(COL_DESCRIPTION, item.description)
                put(COL_EPISODE, item.episode)
                put(COL_RATING, item.rating)
                put(COL_CATEGORY, item.category)
                put(COL_STATUS, item.status)
                put(COL_IS_ANIME, if (item.isAnime) 1 else 0)
                put(COL_IMAGE_URI, item.imageUri)
                put(COL_IS_PINNED, if (item.isPinned) 1 else 0)
            }
            db.insert(TABLE_CONTENT, null, values)
        } catch (e: Exception) {
            -1L
        }
    }

    fun getAllAnime(): MutableList<ContentItem> = getContentByType(true)

    fun getAllDrama(): MutableList<ContentItem> = getContentByType(false)

    private fun getContentByType(isAnime: Boolean): MutableList<ContentItem> {
        val list = mutableListOf<ContentItem>()
        val db = readableDatabase
        var cursor: android.database.Cursor? = null
        try {
            cursor = db.query(
                TABLE_CONTENT, null,
                "$COL_IS_ANIME = ?",
                arrayOf(if (isAnime) "1" else "0"),
                null, null,
                "$COL_IS_PINNED DESC, $COL_ID DESC"
            )
            while (cursor.moveToNext()) {
                list.add(
                    ContentItem(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)) ?: "",
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)) ?: "",
                        episode = cursor.getString(cursor.getColumnIndexOrThrow(COL_EPISODE)) ?: "",
                        rating = cursor.getString(cursor.getColumnIndexOrThrow(COL_RATING)) ?: "5",
                        category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)) ?: "Love",
                        status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)) ?: "Ongoing",
                        isAnime = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ANIME)) == 1,
                        imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_URI)),
                        isPinned = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_PINNED)) == 1
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return list
    }

    fun updateContent(item: ContentItem): Int {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COL_TITLE, item.title)
                put(COL_DESCRIPTION, item.description)
                put(COL_EPISODE, item.episode)
                put(COL_RATING, item.rating)
                put(COL_CATEGORY, item.category)
                put(COL_STATUS, item.status)
                put(COL_IS_ANIME, if (item.isAnime) 1 else 0)
                put(COL_IMAGE_URI, item.imageUri)
                put(COL_IS_PINNED, if (item.isPinned) 1 else 0)
            }
            db.update(TABLE_CONTENT, values, "$COL_ID = ?", arrayOf(item.id.toString()))
        } catch (e: Exception) {
            0
        }
    }

    fun deleteContent(id: Long): Int {
        val db = writableDatabase
        return try {
            db.delete(TABLE_CONTENT, "$COL_ID = ?", arrayOf(id.toString()))
        } catch (e: Exception) {
            0
        }
    }
}