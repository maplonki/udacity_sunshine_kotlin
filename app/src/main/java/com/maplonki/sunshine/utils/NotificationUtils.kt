package com.maplonki.sunshine.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import com.maplonki.sunshine.R
import com.maplonki.sunshine.SunshineDateUtils
import com.maplonki.sunshine.data.SunshinePreferences
import com.maplonki.sunshine.data.db.WeatherContract
import com.maplonki.sunshine.view.activity.DetailActivity

/**
 * Created by hugo on 1/18/18.
 */
class NotificationUtils {

    companion object {
        val WEATHER_NOTIFICATION_ID = 3004
        val WEATHER_NOTIFICATION_PROJECTION = arrayOf(
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
        )

        val NOTIFICATION_CHANNEL_ID = "weather_notification_ch_1"

        @SuppressLint("NewApi")
        fun notifyUserOfNewWeather(context: Context) {
            val todaysWeatherUri = WeatherContract.WeatherEntry.buildWeatherUriWithDate(
                    SunshineDateUtils.normalizeDate(System.currentTimeMillis())
            )

            val todayWeatherCursor: Cursor? = context.contentResolver.query(
                    todaysWeatherUri,
                    WEATHER_NOTIFICATION_PROJECTION,
                    null,
                    null,
                    null
            )

            todayWeatherCursor?.let { cursor ->
                if (cursor.moveToFirst()) {
                    val weatherId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID))
                    val high = cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP))
                    val low = cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP))

                    val largeIconRes = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId)

                    val notificationIcon = BitmapFactory.decodeResource(
                            context.resources,
                            largeIconRes
                    )
                    val notificationTitle = context.getString(R.string.app_name)
                    val notificationText = getNotificationText(context, weatherId, high, low)

                    val smallIconRes = SunshineWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId)

                    val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                            .setSmallIcon(smallIconRes)
                            .setLargeIcon(notificationIcon)
                            .setContentTitle(notificationTitle)
                            .setContentText(notificationText)
                            .setAutoCancel(true)

                    val detailIntent = Intent(context, DetailActivity::class.java).apply {
                        data = todaysWeatherUri
                    }

                    val taskStackBuilder = TaskStackBuilder.create(context).apply {
                        addNextIntentWithParentStack(detailIntent)
                    }

                    val resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                    notificationBuilder.setContentIntent(resultPendingIntent)

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    targetOreo {
                        val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                                "Weather Updates",
                                NotificationManager.IMPORTANCE_DEFAULT
                        ).apply {
                            enableVibration(true)
                        }
                        notificationManager.createNotificationChannel(notificationChannel)
                    }

                    notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build())

                    SunshinePreferences.saveLastNotificationTime(context, System.currentTimeMillis())
                }
                cursor.close()
            }
        }

        fun getNotificationText(context: Context, weatherId: Int, high: Double, low: Double): String {
            val shortDescription = SunshineWeatherUtils.getStringForWeatherCondition(context, weatherId)
            val notificationFormat = context.getString(R.string.format_notification)

            return String.format(
                    notificationFormat,
                    shortDescription,
                    SunshineWeatherUtils.formatTemperature(context, high),
                    SunshineWeatherUtils.formatTemperature(context, low)
            )
        }
    }
}