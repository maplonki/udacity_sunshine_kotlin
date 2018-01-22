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
import com.firebase.jobdispatcher.*
import com.maplonki.sunshine.data.db.WeatherContract
import com.maplonki.sunshine.data.db.WeatherContract.WeatherEntry.Companion.WEATHER_URI
import com.maplonki.sunshine.data.db.WeatherContract.WeatherEntry.Companion._ID
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startService
import java.util.concurrent.TimeUnit

class SunshineSyncUtils {

    companion object {

        private val SYNC_INTERVAL_HOURS = 3L
        private val SYNC_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS).toInt()
        private val SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3

        val SUNSHINE_SYNC_TAG = "sunshine_sync"
        var initialized = false

        @Synchronized
        fun initialize(context: Context) {
            if (initialized) return

            initialized = true

            scheduleFirebaseJobDispatcherSync(context)


            doAsync {
                //Check if we have content on our db first
                val forecastUri = WEATHER_URI

                val projection = arrayOf(_ID)
                val selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards()

                val cursor = context.contentResolver.query(
                        forecastUri,
                        projection,
                        selection,
                        null,
                        null
                )

                if (cursor == null || cursor.count == 0) {
                    startImmediateSync(context)
                }
                cursor.close()

            }
        }

        private fun scheduleFirebaseJobDispatcherSync(context: Context) {
            val firebaseJobDispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))

            val syncSunshineJob: Job = firebaseJobDispatcher.newJobBuilder()
                    .setService(SunshineFirebaseJobService::class.java)
                    .setTag(SUNSHINE_SYNC_TAG)
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    .setLifetime(Lifetime.FOREVER)
                    .setRecurring(true)
                    .setTrigger(Trigger.executionWindow(
                            SYNC_INTERVAL_SECONDS,
                            SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                    .setReplaceCurrent(true)
                    .build()

            firebaseJobDispatcher.schedule(syncSunshineJob)
        }

        private fun startImmediateSync(context: Context) {
            context.startService<SunshineSyncIntentService>()
        }
    }
}