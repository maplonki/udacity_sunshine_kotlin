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

import android.net.Uri
import com.maplonki.sunshine.SunshineDateUtils

class WeatherContract {

    companion object {
        val CONTENT_AUTHORITY = "com.maplonki.sunshine"
        val BASE_CONTENT_URI = Uri.parse("content://${CONTENT_AUTHORITY}")
        val PATH_WEATHER = "weather"
    }

    class WeatherEntry {
        companion object {
            /* The base CONTENT_URI used to query the Weather table from the content provider */
            val WEATHER_URI = BASE_CONTENT_URI.buildUpon()
                    .appendPath(PATH_WEATHER)
                    .build()

            /* Used internally as the name of our weather table. */
            val TABLE_NAME = "weather"

            val _ID = "_id"

            val COLUMN_DATE = "date"

            /* Weather ID as returned by API, used to identify the icon to be used */
            val COLUMN_WEATHER_ID = "weather_id"

            /* Min and max temperatures in °C for the day (stored as floats in the database) */
            val COLUMN_MIN_TEMP = "min"
            val COLUMN_MAX_TEMP = "max"

            /* Humidity is stored as a float representing percentage */
            val COLUMN_HUMIDITY = "humidity"

            /* Pressure is stored as a float representing percentage */
            val COLUMN_PRESSURE = "pressure"

            /* Wind speed is stored as a float representing wind speed in mph */
            val COLUMN_WIND_SPEED = "wind"


            val COLUMN_DEGREES = "degrees"

            fun buildWeatherUriWithDate(date: Long): Uri {
                return WEATHER_URI.buildUpon()
                        .appendPath(date.toString())
                        .build()
            }

            /**
             * Returns just the selection part of the weather query from a normalized today value.
             * This is used to get a weather forecast from today's date. To make this easy to use
             * in compound selection, we embed today's date as an argument in the query.
             *
             * @return The selection part of the weather query for today onwards
             */
            fun getSqlSelectForTodayOnwards(): String {
                val normalizedUtcNow = SunshineDateUtils.normalizeDate(System.currentTimeMillis())
                return "$COLUMN_DATE >= $normalizedUtcNow"
            }
        }
    }

}