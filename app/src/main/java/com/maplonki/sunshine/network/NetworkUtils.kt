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
package com.maplonki.sunshine.network

import android.content.Context
import android.net.Uri
import com.maplonki.sunshine.data.SunshinePreferences
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class NetworkUtils {

    companion object {
        val TAG = NetworkUtils::class.java.name

        /* returns a different forecast everytime its refreshed */
        val DYNAMIC_WEATHER_URL = "https://andfun-weather.udacity.com/weather"

        /* returns the actual forecast */
        val STATIC_WEATHER_URL = "https://andfun-weather.udacity.com/staticweather"

        val FORECAST_BASE_URL = DYNAMIC_WEATHER_URL

        val format = "json"
        val units = "metric"
        /* the number of days for the forecast */
        val numDays = 14

        val QUERY_PARAM = "q"
        val LAT_PARAM = "lat"
        val LON_PARAM = "lon"

        val FORMAT_PARAM = "mode"
        val UNITS_PARAM = "units"
        //count
        val DAYS_PARAM = "cnt"

        fun getUrl(context: Context): URL? {
            return if (SunshinePreferences.isLocationLatLonAvailable(context)) {
                val (latitude, longitude) = SunshinePreferences.getLocationCoordinates(context)
                buildUrlWithLatitudeLongitude(latitude, longitude)
            } else {
                val locationQuery = SunshinePreferences.getPreferredWeatherLocation(context)
                buildUrlWithLocationQuery(locationQuery)
            }
        }

        private fun buildUrlWithLatitudeLongitude(lat: Double, lon: Double): URL? {
            val weatherQueryUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(LAT_PARAM, lat.toString())
                    .appendQueryParameter(LON_PARAM, lon.toString())
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, numDays.toString())
                    .build()

            return try {
                URL(weatherQueryUri.toString())
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                null
            }

        }

        private fun buildUrlWithLocationQuery(locationQuery: String): URL? {
            val weatherQueryUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationQuery)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .build()

            return try {
                URL(weatherQueryUri.toString())
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                null
            }

        }

        @Throws(IOException::class)
        fun getResponseFromHttpUrl(url: URL): String? {
            val urlConnection = url.openConnection() as HttpURLConnection
            return try {
                val inputStream = urlConnection.inputStream

                val scanner = Scanner(inputStream).apply {
                    useDelimiter("\\A")
                }

                val hasInput = scanner.hasNext()
                var response: String? = null
                if (hasInput) {
                    response = scanner.next()
                }
                scanner.close()
                response
            } finally {
                urlConnection.disconnect()
            }
        }

    }
}