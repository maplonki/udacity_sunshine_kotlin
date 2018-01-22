package com.maplonki.sunshine.network

import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import org.jetbrains.anko.doAsync

/**
 * Created by hugo on 1/14/18.
 */
class SunshineFirebaseJobService : JobService() {

    override fun onStartJob(jobParams: JobParameters?): Boolean {
        doAsync {
            val context = applicationContext
            SunshineSyncTask.syncWeather(context)
            jobFinished(jobParams!!, false)
        }
        return true
    }

    override fun onStopJob(job: JobParameters?): Boolean {
        return true
    }
}