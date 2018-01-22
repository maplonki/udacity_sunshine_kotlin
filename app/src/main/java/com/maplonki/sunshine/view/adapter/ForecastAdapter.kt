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
package com.maplonki.sunshine.view.adapter

import android.content.Context
import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.maplonki.sunshine.R
import com.maplonki.sunshine.SunshineDateUtils
import com.maplonki.sunshine.data.db.WeatherContract
import com.maplonki.sunshine.extensions.get
import com.maplonki.sunshine.extensions.inflate
import com.maplonki.sunshine.utils.SunshineWeatherUtils
import org.jetbrains.anko.find

class ForecastAdapter(val context: Context,
                      var cursor: Cursor? = null,
                      val onClick: (date: Long) -> Unit) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    companion object {
        val VIEW_TYPE_TODAY = 0
        val VIEW_TYPE_FUTURE = 1
    }

    //Landscape configuration
    private var useTodayLayout = context.resources.getBoolean(R.bool.use_today_layout)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ForecastViewHolder {
        val layoutId = when (viewType) {
            VIEW_TYPE_TODAY -> R.layout.list_item_forecast_today
            VIEW_TYPE_FUTURE -> R.layout.forecast_list_item
            else -> throw IllegalArgumentException("Invalid view of type $viewType")
        }

        return ForecastViewHolder(context.inflate(layoutId, parent))
    }

    override fun onBindViewHolder(viewHolder: ForecastViewHolder?, position: Int) {

        cursor?.let { cursor ->
            viewHolder?.let { viewHolder ->
                cursor.moveToPosition(position)

                /****************
                 * Weather Icon *
                 ****************/
                val weatherId = cursor.get<Int>(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID)

                val viewType = getItemViewType(position)

                val weatherImageId = when (viewType) {
                    VIEW_TYPE_TODAY -> SunshineWeatherUtils
                            .getLargeArtResourceIdForWeatherCondition(weatherId)

                    VIEW_TYPE_FUTURE -> SunshineWeatherUtils
                            .getSmallArtResourceIdForWeatherCondition(weatherId)

                    else -> throw IllegalArgumentException("Invalid view type, value of $viewType")
                }

                viewHolder.iconView.setImageResource(weatherImageId)

                /****************
                 * Weather Date *
                 ****************/
                /* Read date from the cursor */
                val dateInMillis = cursor.get<Long>(WeatherContract.WeatherEntry.COLUMN_DATE)
                /* Get human readable string using our utility method */
                val dateString = SunshineDateUtils.getFriendlyDateString(context, dateInMillis, false)

                /* Display friendly date string */
                viewHolder.dateView.text = dateString

                /***********************
                 * Weather Description *
                 ***********************/
                val description = SunshineWeatherUtils.getStringForWeatherCondition(context, weatherId)
                /* Create the accessibility (a11y) String from the weather description */
                val descriptionA11y = context.getString(R.string.a11y_forecast, description)

                /* Set the text and content description (for accessibility purposes) */
                viewHolder.descriptionView.text = description
                viewHolder.descriptionView.contentDescription = descriptionA11y

                /**************************
                 * High (max) temperature *
                 **************************/
                /* Read high temperature from the cursor (in degrees celsius) */
                val highInCelsius = cursor.get<Double>(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)
                /*
                 * If the user's preference for weather is fahrenheit, formatTemperature will convert
                 * the temperature. This method will also append either °C or °F to the temperature
                 * String.
                 */
                val highString = SunshineWeatherUtils.formatTemperature(context, highInCelsius)
                /* Create the accessibility (a11y) String from the weather description */
                val highA11y = context.getString(R.string.a11y_high_temp, highString)

                /* Set the text and content description (for accessibility purposes) */
                viewHolder.highTempView.text = highString
                viewHolder.highTempView.contentDescription = highA11y

                /*************************
                 * Low (min) temperature *
                 *************************/
                /* Read low temperature from the cursor (in degrees celsius) */
                val lowInCelsius = cursor.get<Double>(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)
                /*
                  * If the user's preference for weather is fahrenheit, formatTemperature will convert
                  * the temperature. This method will also append either °C or °F to the temperature
                  * String.
                  */
                val lowString = SunshineWeatherUtils.formatTemperature(context, lowInCelsius)
                val lowA11y = context.getString(R.string.a11y_low_temp, lowString)

                /* Set the text and content description (for accessibility purposes) */
                viewHolder.lowTempView.text = lowString
                viewHolder.lowTempView.contentDescription = lowA11y
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (useTodayLayout && position == 0) {
            VIEW_TYPE_TODAY
        } else {
            VIEW_TYPE_FUTURE
        }
    }

    override fun getItemCount() = cursor?.count ?: 0

    fun swapCursor(newCursor: Cursor?) {
        cursor = newCursor
        notifyDataSetChanged()
    }

    inner class ForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /* using this method cause the synthetic import would
         * yield different references when landscape vs portrait layouts*/
        val iconView = view.find<ImageView>(R.id.weather_icon)
        val dateView = view.find<TextView>(R.id.date)
        val descriptionView = view.find<TextView>(R.id.weather_description)
        val highTempView = view.find<TextView>(R.id.high_temperature)
        val lowTempView = view.find<TextView>(R.id.low_temperature)

        init {
            view.setOnClickListener {
                cursor?.let {
                    it.moveToPosition(adapterPosition)
                    val dateInMillis = it.get<Long>(WeatherContract.WeatherEntry.COLUMN_DATE)
                    onClick(dateInMillis)
                }
            }
        }
    }

}