package com.maplonki.sunshine.network

import android.content.Context
import android.text.format.DateUtils
import com.maplonki.sunshine.data.SunshinePreferences
import com.maplonki.sunshine.data.db.WeatherContract.WeatherEntry.Companion.WEATHER_URI
import com.maplonki.sunshine.network.json.OpenWeatherJsonUtils
import com.maplonki.sunshine.utils.NotificationUtils
import java.net.URL

/**
 * Created by hugo on 1/15/18.
 */
class SunshineSyncTask {

    companion object {

        /**
         * Performs the network request for updated weather, parses the JSON from that request, and
         * inserts the new weather information into our ContentProvider. Will notify the user that new
         * weather has been loaded if the user hasn't been notified of the weather within the last day
         * AND they haven't disabled notifications in the preferences screen.
         *
         * @param context Used to access utility methods and the ContentResolver
         */
        fun syncWeather(context: Context) {
            try {
                val weatherRequestUrl = NetworkUtils.getUrl(context)

                val jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl ?: URL(""))

                val weatherValues = OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context, jsonWeatherResponse!!)

                weatherValues?.let {
                    if (it.isNotEmpty()) {
                        val sunshineResolver = context.contentResolver

                        /* delete old weather data */
                        sunshineResolver.delete(
                                WEATHER_URI,
                                null,
                                null
                        )

                        sunshineResolver.bulkInsert(
                                WEATHER_URI,
                                it
                        )

                        val notificationsEnabled = SunshinePreferences.areNotificationsEnabled(context)

                        /* if the last notification was shown more than 1 day ago, we want to send another notification
                        * that the weather has been updated*/
                        val timeSinceLastNotification = SunshinePreferences.getEllapsedTimeSinceLastNotification(context)
                        val oneDayPassedSinceLastNotification = timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS

                        if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                            NotificationUtils.notifyUserOfNewWeather(context)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}