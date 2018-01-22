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
package com.maplonki.sunshine.view.activity

import android.content.Intent
import android.database.Cursor
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.maplonki.sunshine.R
import com.maplonki.sunshine.data.SunshinePreferences
import com.maplonki.sunshine.data.db.WeatherContract
import com.maplonki.sunshine.data.db.WeatherContract.WeatherEntry.Companion.COLUMN_DATE
import com.maplonki.sunshine.data.db.WeatherContract.WeatherEntry.Companion.COLUMN_MAX_TEMP
import com.maplonki.sunshine.data.db.WeatherContract.WeatherEntry.Companion.COLUMN_MIN_TEMP
import com.maplonki.sunshine.data.db.WeatherContract.WeatherEntry.Companion.COLUMN_WEATHER_ID
import com.maplonki.sunshine.data.db.WeatherContract.WeatherEntry.Companion.WEATHER_URI
import com.maplonki.sunshine.databinding.ActivityForecastBinding
import com.maplonki.sunshine.extensions.startActivity
import com.maplonki.sunshine.network.SunshineSyncUtils
import com.maplonki.sunshine.view.adapter.ForecastAdapter
import com.maplonki.sunshine.view.viewmodel.ForecastViewModel

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private val mForecastAdapter by lazy {
        ForecastAdapter(this, onClick = onForecastClick)
    }

    lateinit var mForecastBinding: ActivityForecastBinding
    private val mRecyclerView by lazy { mForecastBinding.recyclerviewForecast }

    private val onForecastClick: (date: Long) -> Unit = { date ->
        val detailIntent = Intent(this@MainActivity, DetailActivity::class.java).apply {
            val uriForDate = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date)
            data = uriForDate
        }
        startActivity(detailIntent)
    }

    private var position = RecyclerView.NO_POSITION

    companion object {
        val ID_FORECAST_LOADER = 44

        /* used by the cursor loader to query the list of
         * forecasts */
        val MAIN_FORECAST_PROJECTION = arrayOf(
                COLUMN_DATE,
                COLUMN_MAX_TEMP,
                COLUMN_MIN_TEMP,
                COLUMN_WEATHER_ID
        )

        val TAG: String = MainActivity::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mForecastBinding = DataBindingUtil.setContentView(this, R.layout.activity_forecast)
        mForecastBinding.forecast = ForecastViewModel()

        supportActionBar?.elevation = 0f

        mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            adapter = mForecastAdapter
        }

        supportLoaderManager.initLoader(ID_FORECAST_LOADER, null, this)
        SunshineSyncUtils.initialize(this)
    }

    fun openPreferredLocationInMap() {
        val (lat, lon) = SunshinePreferences.getLocationCoordinates(this)
        val geoLocation = Uri.parse("geo:$lat,$lon")
        startActivity(Intent.ACTION_VIEW, geoLocation) {
            Log.d(TAG, "Couldn't call $geoLocation, no receiving apps installed")
        }
    }

    /* Loader Callbacks */
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return when (id) {
            ID_FORECAST_LOADER -> {
                val forecastQuery = WEATHER_URI
                val sortOrder = "$COLUMN_DATE ASC"

                val selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards()

                CursorLoader(
                        this,
                        forecastQuery,
                        MAIN_FORECAST_PROJECTION,
                        selection,
                        null,
                        sortOrder
                )
            }
            else -> {
                throw RuntimeException("Loader Not Implemented $id")
            }
        }
    }


    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        mForecastAdapter.swapCursor(data)
        if (data?.count != 0) {
            mForecastBinding.forecast?.isLoading = false
        }

        if (position == RecyclerView.NO_POSITION) position = 0
        mRecyclerView.smoothScrollToPosition(position)

    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
        mForecastAdapter.swapCursor(null)
    }

}