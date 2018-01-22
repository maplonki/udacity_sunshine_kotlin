/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.maplonki.sunshine.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class WeatherDbHelper(context: Context) : ManagedSQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        val DATABASE_NAME = "weather.db"
        val DATABASE_VERSION = 1

        private var instance: WeatherDbHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): WeatherDbHelper {
            if (instance == null) {
                instance = WeatherDbHelper(ctx)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {

        // Create the table

        db?.createTable(WeatherContract.WeatherEntry.TABLE_NAME, true,
                WeatherContract.WeatherEntry._ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,

                //If there's another entry with the same date, just replace it
                WeatherContract.WeatherEntry.COLUMN_DATE to INTEGER + NOT_NULL + UNIQUE(ConflictClause.REPLACE),
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID to INTEGER + NOT_NULL,

                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP to REAL + NOT_NULL,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP to REAL + NOT_NULL,

                WeatherContract.WeatherEntry.COLUMN_HUMIDITY to REAL + NOT_NULL,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE to REAL + NOT_NULL,

                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED to REAL + NOT_NULL,
                WeatherContract.WeatherEntry.COLUMN_DEGREES to REAL + NOT_NULL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.dropTable(WeatherContract.WeatherEntry.TABLE_NAME, true)
        onCreate(db)
    }
}