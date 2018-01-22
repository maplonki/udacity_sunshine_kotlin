package com.maplonki.sunshine.model.db

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.maplonki.sunshine.SunshineDateUtils
import com.maplonki.sunshine.data.db.WeatherContract
import com.maplonki.sunshine.data.db.WeatherContract.Companion.CONTENT_AUTHORITY
import com.maplonki.sunshine.data.db.WeatherContract.WeatherEntry.Companion.COLUMN_DATE
import com.maplonki.sunshine.data.db.WeatherContract.WeatherEntry.Companion.TABLE_NAME
import com.maplonki.sunshine.data.db.WeatherDbHelper

/**
 * Created by hugo on 1/11/18.
 */
class WeatherProvider : ContentProvider() {

    private val dbHelper by lazy { WeatherDbHelper.getInstance(context) }

    companion object {
        val CODE_WEATHER = 100
        val CODE_WEATHER_WITH_DATE = 101

        private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER, CODE_WEATHER)
            addURI(CONTENT_AUTHORITY, "${WeatherContract.PATH_WEATHER}/#", CODE_WEATHER_WITH_DATE)
        }
    }

    override fun onCreate() = true

    override fun bulkInsert(uri: Uri?, values: Array<out ContentValues>?): Int {

        return when (uriMatcher.match(uri)) {
            CODE_WEATHER -> {
                var rowsInserted = 0
                values?.forEach { value ->
                    val weatherDate = value.getAsLong(COLUMN_DATE)

                    if (!SunshineDateUtils.isDateNormalized(weatherDate))
                        throw IllegalArgumentException("Date must be normalized to insert")

                    val result = dbHelper.writableDatabase.insert(TABLE_NAME, null, value)

                    if (result != -1L) rowsInserted++
                }
                if (rowsInserted > 0) context.contentResolver.notifyChange(uri, null)

                rowsInserted
            }
            else -> super.bulkInsert(uri, values)
        }
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {

        return when (uriMatcher.match(uri)) {
            CODE_WEATHER_WITH_DATE -> {
                val normalizedDateString = uri?.lastPathSegment
                val selectionArguments = arrayOf(normalizedDateString)

                dbHelper.readableDatabase.query(
                        TABLE_NAME,
                        projection,
                        "$COLUMN_DATE = ?",
                        selectionArguments,
                        null,
                        null,
                        sortOrder
                )
            }

            CODE_WEATHER -> {
                dbHelper.readableDatabase.query(
                        TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                )
            }

            else -> throw UnsupportedOperationException("Unknown uri: $uri")

        }.apply {
            //we notify that we've retrieved content from the provider
            setNotificationUri(context.contentResolver, uri)
        }
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        val selectionVar = selection ?: "1"

        val numRowsDeleted: Int = when (uriMatcher.match(uri)) {
            CODE_WEATHER -> {
                dbHelper.writableDatabase.delete(
                        TABLE_NAME,
                        selectionVar,
                        selectionArgs
                )
            }

            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }

        if (numRowsDeleted != 0) {
            context.contentResolver.notifyChange(uri, null)
        }
        return numRowsDeleted
    }


    override fun getType(p0: Uri?): String {
        throw RuntimeException("We are not implementing getType in Sunshine.")
    }

    override fun insert(p0: Uri?, p1: ContentValues?): Uri {
        throw RuntimeException("We are not implementing insert in Sunshine. Use bulkInsert instead")
    }

    override fun update(p0: Uri?, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        throw RuntimeException("We are not implementing update in Sunshine")
    }

    override fun shutdown() {
        super.shutdown()
        dbHelper.close()
    }


}