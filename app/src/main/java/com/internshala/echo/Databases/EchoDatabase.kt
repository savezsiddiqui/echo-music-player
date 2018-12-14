package com.internshala.echo.Databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.internshala.echo.Databases.EchoDatabase.Staticated.COLUMN_ID
import com.internshala.echo.Databases.EchoDatabase.Staticated.COLUMN_SONG_ARTIST
import com.internshala.echo.Databases.EchoDatabase.Staticated.COLUMN_SONG_PATH
import com.internshala.echo.Databases.EchoDatabase.Staticated.COLUMN_SONG_TITLE
import com.internshala.echo.Databases.EchoDatabase.Staticated.DATABASE_NAME
import com.internshala.echo.Databases.EchoDatabase.Staticated.DB_VERSION
import com.internshala.echo.Databases.EchoDatabase.Staticated.TABLE_NAME
import com.internshala.echo.Songs

class EchoDatabase : SQLiteOpenHelper {



    var songList = ArrayList<Songs>()


    object Staticated {
        val DATABASE_NAME = "FavouritesDatabase"
        var DB_VERSION = 1
        val TABLE_NAME = "FavouritesTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_PATH = "SongData"
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version)
    constructor(context: Context?) : super(context, DATABASE_NAME, null, DB_VERSION)


    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
        sqLiteDatabase?.execSQL("CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER," + COLUMN_SONG_ARTIST + " STRING,"
                + COLUMN_SONG_TITLE + " STRING," + COLUMN_SONG_PATH + " STRING);")

    }

    fun storeASFavourite(id: Int, artist: String, title: String, path: String) {

        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(COLUMN_ID, id)
        contentValues.put(COLUMN_SONG_TITLE, title)
        contentValues.put(COLUMN_SONG_ARTIST, artist)
        contentValues.put(COLUMN_SONG_PATH, path)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }


    fun queryDBlist(): ArrayList<Songs>? {

        try {
            val db = this.readableDatabase
            val query_params = "SELECT * FROM " + TABLE_NAME
            var cursor = db.rawQuery(query_params, null)
            if (cursor.moveToFirst()) {
                do {
                    var id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                    var title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SONG_TITLE))
                    var artist = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SONG_ARTIST))
                    var path = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SONG_PATH))
                    songList.add(Songs(id.toLong(), title, artist, path, 0))
                } while (cursor.moveToNext())
            } else {
                return null
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songList
    }

    fun checkIfIdExists(id: Int): Boolean {
        var storeId = -911
        var db = this.readableDatabase
        var query_params = "SELECT * FROM " + TABLE_NAME + " WHERE SongId = '$id'"
        var cursor = db.rawQuery(query_params, null)
        if (cursor.moveToNext()) {
            do {
                storeId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            } while (cursor.moveToNext())
        } else {
            return false
        }
        return storeId != -911
    }

    fun deleteFavourite(id: Int) {
        var db = this.writableDatabase
        db.delete(TABLE_NAME, COLUMN_ID + " = " + id, null)
        db.close()
    }

    fun checksize(): Int {

        var count = 0
        var db = this.readableDatabase
        var query_params = "SELECT * FROM " + TABLE_NAME
        var cursor = db.rawQuery(query_params, null)
        if (cursor.moveToNext()) {
            do {
                ++count
            } while (cursor.moveToNext())
        } else
            return 0
        return count
    }
}