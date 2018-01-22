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
package com.maplonki.sunshine.utils

import android.content.Context
import android.util.Log
import com.maplonki.sunshine.R

class SunshineWeatherUtils {

    companion object {

        private val LOG_TAG = SunshineWeatherUtils::class.java.simpleName

        /**
         * This method will convert a temperature from Celsius to Fahrenheit.
         *
         * @param temperatureInCelsius Temperature in degrees Celsius(°C)
         *
         * @return Temperature in degrees Fahrenheit (°F)
         */
        private fun celsiusToFahrenheit(temperatureInCelsius: Double): Double {
            return temperatureInCelsius * 1.8 + 32
        }

        /**
         * Temperature data is stored in Celsius by our app. Depending on the user's preference,
         * the app may need to display the temperature in Fahrenheit. This method will perform that
         * temperature conversion if necessary. It will also format the temperature so that no
         * decimal points show. Temperatures will be formatted to the following form: "21°"
         *
         * @param context     Android Context to access preferences and resources
         * @param temperature Temperature in degrees Celsius (°C)
         *
         * @return Formatted temperature String in the following form:
         * "21°"
         */
        fun formatTemperature(context: Context, temperature: Double): String {
            var temperature = temperature
            /*if (!SunshinePreferences.isMetric(context)) {
                temperature = celsiusToFahrenheit(temperature)
            }*/

            val temperatureFormatResourceId = R.string.format_temperature

            /* For presentation, assume the user doesn't care about tenths of a degree. */
            return String.format(context.getString(temperatureFormatResourceId), temperature)
        }

        /**
         * This method will format the temperatures to be displayed in the
         * following form: "HIGH° / LOW°"
         *
         * @param context Android Context to access preferences and resources
         * @param high    High temperature for a day in user's preferred units
         * @param low     Low temperature for a day in user's preferred units
         *
         * @return String in the form: "HIGH° / LOW°"
         */
        fun formatHighLows(context: Context, high: Double, low: Double): String {
            val roundedHigh = Math.round(high)
            val roundedLow = Math.round(low)

            val formattedHigh = formatTemperature(context, roundedHigh.toDouble())
            val formattedLow = formatTemperature(context, roundedLow.toDouble())

            return formattedHigh + " / " + formattedLow
        }

        /**
         * This method uses the wind direction in degrees to determine compass direction as a
         * String. (eg NW) The method will return the wind String in the following form: "2 km/h SW"
         *
         * @param context   Android Context to access preferences and resources
         * @param windSpeed Wind speed in kilometers / hour
         * @param degrees   Degrees as measured on a compass, NOT temperature degrees!
         * See https://www.mathsisfun.com/geometry/degrees.html
         *
         * @return Wind String in the following form: "2 km/h SW"
         */
        fun getFormattedWind(context: Context, windSpeed: Float, degrees: Float): String {
            val windFormat = R.string.format_wind_kmh

            /*if (!SunshinePreferences.isMetric(context)) {
                windFormat = R.string.format_wind_mph
                windSpeed = .621371192237334f * windSpeed
            }*/

            /*
             * You know what's fun? Writing really long if/else statements with tons of possible
             * conditions. Seriously, try it!
             */
            val direction = when {
                degrees >= 337.5 || degrees < 22.5 -> "N"
                degrees in 22.5..67.4 -> "NE"
                degrees in 67.5..112.4 -> "E"
                degrees in 112.5..157.4 -> "SE"
                degrees in 157.5..202.4 -> "S"
                degrees in 202.5..247.4 -> "SW"
                degrees in 247.5..292.4 -> "W"
                degrees in 292.5..337.4 -> "NW"
                else -> "Unknown"
            }

            return String.format(context.getString(windFormat), windSpeed, direction)
        }

        /**
         * Helper method to provide the string according to the weather
         * condition id returned by the OpenWeatherMap call.
         *
         * @param context   Android context
         * @param weatherId from OpenWeatherMap API response
         * See http://openweathermap.org/weather-conditions for a list of all IDs
         *
         * @return String for the weather condition, null if no relation is found.
         */
        fun getStringForWeatherCondition(context: Context, weatherId: Int): String {
            val stringId: Int
            if (weatherId in 200..232) {
                stringId = R.string.condition_2xx
            } else if (weatherId in 300..321) {
                stringId = R.string.condition_3xx
            } else
                stringId = when (weatherId) {
                    500 -> R.string.condition_500
                    501 -> R.string.condition_501
                    502 -> R.string.condition_502
                    503 -> R.string.condition_503
                    504 -> R.string.condition_504
                    511 -> R.string.condition_511
                    520 -> R.string.condition_520
                    531 -> R.string.condition_531
                    600 -> R.string.condition_600
                    601 -> R.string.condition_601
                    602 -> R.string.condition_602
                    611 -> R.string.condition_611
                    612 -> R.string.condition_612
                    615 -> R.string.condition_615
                    616 -> R.string.condition_616
                    620 -> R.string.condition_620
                    621 -> R.string.condition_621
                    622 -> R.string.condition_622
                    701 -> R.string.condition_701
                    711 -> R.string.condition_711
                    721 -> R.string.condition_721
                    731 -> R.string.condition_731
                    741 -> R.string.condition_741
                    751 -> R.string.condition_751
                    761 -> R.string.condition_761
                    762 -> R.string.condition_762
                    771 -> R.string.condition_771
                    781 -> R.string.condition_781
                    800 -> R.string.condition_800
                    801 -> R.string.condition_801
                    802 -> R.string.condition_802
                    803 -> R.string.condition_803
                    804 -> R.string.condition_804
                    900 -> R.string.condition_900
                    901 -> R.string.condition_901
                    902 -> R.string.condition_902
                    903 -> R.string.condition_903
                    904 -> R.string.condition_904
                    905 -> R.string.condition_905
                    906 -> R.string.condition_906
                    951 -> R.string.condition_951
                    952 -> R.string.condition_952
                    953 -> R.string.condition_953
                    954 -> R.string.condition_954
                    955 -> R.string.condition_955
                    956 -> R.string.condition_956
                    957 -> R.string.condition_957
                    958 -> R.string.condition_958
                    959 -> R.string.condition_959
                    960 -> R.string.condition_960
                    961 -> R.string.condition_961
                    962 -> R.string.condition_962
                    else -> return context.getString(R.string.condition_unknown, weatherId)
                }

            return context.getString(stringId)
        }

        /**
         * Helper method to provide the icon resource id according to the weather condition id returned
         * by the OpenWeatherMap call. This method is very similar to
         *
         * [.getLargeArtResourceIdForWeatherCondition].
         *
         * The difference between these two methods is that this method provides smaller assets, used
         * in the list item layout for a "future day", as well as
         *
         * @param weatherId from OpenWeatherMap API response
         * See http://openweathermap.org/weather-conditions for a list of all IDs
         *
         * @return resource id for the corresponding icon. -1 if no relation is found.
         */
        fun getSmallArtResourceIdForWeatherCondition(weatherId: Int): Int {

            /*
             * Based on weather code data for Open Weather Map.
             */
            if (weatherId in 200..232) {
                return R.drawable.ic_storm
            } else if (weatherId in 300..321) {
                return R.drawable.ic_light_rain
            } else if (weatherId in 500..504) {
                return R.drawable.ic_rain
            } else if (weatherId == 511) {
                return R.drawable.ic_snow
            } else if (weatherId in 520..531) {
                return R.drawable.ic_rain
            } else if (weatherId in 600..622) {
                return R.drawable.ic_snow
            } else if (weatherId in 701..761) {
                return R.drawable.ic_fog
            } else if (weatherId == 761 || weatherId == 771 || weatherId == 781) {
                return R.drawable.ic_storm
            } else if (weatherId == 800) {
                return R.drawable.ic_clear
            } else if (weatherId == 801) {
                return R.drawable.ic_light_clouds
            } else if (weatherId in 802..804) {
                return R.drawable.ic_cloudy
            } else if (weatherId in 900..906) {
                return R.drawable.ic_storm
            } else if (weatherId in 958..962) {
                return R.drawable.ic_storm
            } else if (weatherId in 951..957) {
                return R.drawable.ic_clear
            }

            Log.e(LOG_TAG, "Unknown Weather: " + weatherId)
            return R.drawable.ic_storm
        }

        /**
         * Helper method to provide the art resource ID according to the weather condition ID returned
         * by the OpenWeatherMap call. This method is very similar to
         *
         * [.getSmallArtResourceIdForWeatherCondition].
         *
         * The difference between these two methods is that this method provides larger assets, used
         * in the "today view" of the list, as well as in the DetailActivity.
         *
         * @param weatherId from OpenWeatherMap API response
         * See http://openweathermap.org/weather-conditions for a list of all IDs
         *
         * @return resource ID for the corresponding icon. -1 if no relation is found.
         */
        fun getLargeArtResourceIdForWeatherCondition(weatherId: Int): Int {

            /*
             * Based on weather code data for Open Weather Map.
             */
            if (weatherId >= 200 && weatherId <= 232) {
                return R.drawable.art_storm
            } else if (weatherId >= 300 && weatherId <= 321) {
                return R.drawable.art_light_rain
            } else if (weatherId >= 500 && weatherId <= 504) {
                return R.drawable.art_rain
            } else if (weatherId == 511) {
                return R.drawable.art_snow
            } else if (weatherId >= 520 && weatherId <= 531) {
                return R.drawable.art_rain
            } else if (weatherId >= 600 && weatherId <= 622) {
                return R.drawable.art_snow
            } else if (weatherId >= 701 && weatherId <= 761) {
                return R.drawable.art_fog
            } else if (weatherId == 761 || weatherId == 771 || weatherId == 781) {
                return R.drawable.art_storm
            } else if (weatherId == 800) {
                return R.drawable.art_clear
            } else if (weatherId == 801) {
                return R.drawable.art_light_clouds
            } else if (weatherId >= 802 && weatherId <= 804) {
                return R.drawable.art_clouds
            } else if (weatherId >= 900 && weatherId <= 906) {
                return R.drawable.art_storm
            } else if (weatherId >= 958 && weatherId <= 962) {
                return R.drawable.art_storm
            } else if (weatherId >= 951 && weatherId <= 957) {
                return R.drawable.art_clear
            }

            Log.e(LOG_TAG, "Unknown Weather: " + weatherId)
            return R.drawable.art_storm
        }
    }
}