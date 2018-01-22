package com.maplonki.sunshine.extensions

import android.database.Cursor

/**
 * Created by hugo on 1/18/18.
 */
inline fun <reified T> Cursor.get(columnName: String): T {
    val columnIndex = getColumnIndex(columnName)
    return when (T::class) {
        Int::class -> getInt(columnIndex) as T
        Long::class -> getLong(columnIndex) as T
        String::class -> getString(columnIndex) as T
        Double::class -> getDouble(columnIndex) as T
        Float::class -> getFloat(columnIndex) as T
        else -> throw IllegalArgumentException("Unsupported type ${T::class}")
    }
}