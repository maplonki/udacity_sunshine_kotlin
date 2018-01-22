package com.maplonki.sunshine.view.viewmodel

import android.content.Context
import android.database.Cursor
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.BindingAdapter
import android.widget.ImageView
import com.maplonki.sunshine.BR
import com.maplonki.sunshine.R
import com.maplonki.sunshine.SunshineDateUtils
import com.maplonki.sunshine.data.db.WeatherContract
import com.maplonki.sunshine.extensions.get
import com.maplonki.sunshine.extensions.notify
import com.maplonki.sunshine.utils.SunshineWeatherUtils

/**
 * Created by hugo on 1/20/18.
 */
class ForecastDetailViewModel(val context: Context) : BaseObservable() {

    companion object {
        @JvmStatic
        @BindingAdapter("setIcon")
        fun setForecastIcon(imageView: ImageView, iconRes: Int) {
            val icon = imageView.context.resources.getDrawable(iconRes)
            imageView.setImageDrawable(icon)
        }
    }


    var cursor: Cursor? = null
        set(value) {
            field = value
            notify(BR.icon, BR.date, BR.description, BR.high, BR.low,
                    BR.humidity, BR.pressure, BR.wind)
        }

    var icon = R.drawable.art_clear
        @Bindable
        get() {
            val weatherId = cursor?.get<Int>(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID) ?: 0
            return SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId)
        }

    val date: String
        @Bindable
        get() {
            val dateMillis = cursor?.get<Long>(WeatherContract.WeatherEntry.COLUMN_DATE) ?: 0
            return SunshineDateUtils.getFriendlyDateString(context, dateMillis, true)
        }

    val description: String
        @Bindable
        get() {
            val weatherId = cursor?.get<Int>(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID) ?: 0
            return SunshineWeatherUtils.getStringForWeatherCondition(context, weatherId)
        }

    val high: String
        @Bindable
        get() {
            val highInCelsius = cursor?.get<Double>(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP) ?: 0.0
            return SunshineWeatherUtils.formatTemperature(context, highInCelsius)
        }

    val low: String
        @Bindable
        get() {
            val minInCelsius = cursor?.get<Double>(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP) ?: 0.0
            return SunshineWeatherUtils.formatTemperature(context, minInCelsius)
        }

    val humidity: String
        @Bindable
        get() {
            val humidity = cursor?.get<Float>(WeatherContract.WeatherEntry.COLUMN_HUMIDITY) ?: 0F
            return context.getString(R.string.format_humidity, humidity)
        }

    val pressure: String
        @Bindable
        get() {
            val pressure = cursor?.get<Float>(WeatherContract.WeatherEntry.COLUMN_PRESSURE) ?: 0F
            return context.getString(R.string.format_pressure, pressure)
        }

    val wind: String
        @Bindable
        get() {
            val windSpeed = cursor?.get<Float>(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED) ?: 0F
            val windDirection = cursor?.get<Float>(WeatherContract.WeatherEntry.COLUMN_DEGREES) ?: 0F
            return SunshineWeatherUtils.getFormattedWind(context, windSpeed, windDirection)
        }


}