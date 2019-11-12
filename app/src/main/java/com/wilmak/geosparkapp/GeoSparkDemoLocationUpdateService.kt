package com.wilmak.geosparkapp

import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

class GeoSparkDemoLocationUpdateService : JobService() {

    private lateinit var mLocationReceiver : GeoSparkDemoReceiver

    override fun onCreate() {
        super.onCreate()
        mLocationReceiver = GeoSparkDemoReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        try {
            unregisterReceiver(mLocationReceiver)
            locationJob(this)
        } catch (ex: Exception) {}
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        try {
            registerReceiver(mLocationReceiver, IntentFilter("com.geospark.android.RECEIVED"))
        } catch (ex: Exception) {}
        return true
    }

    companion object {
        fun locationJob(context: Context) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                try {
                    val jobScheduler =
                        context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler;
                    val job = JobInfo.Builder(
                        1001,
                        ComponentName(context, GeoSparkDemoLocationUpdateService::class.java)
                    )
                        .setMinimumLatency(1000)
                        .setOverrideDeadline(1000)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                        .setPersisted(true)
                        .build();
                    jobScheduler.schedule(job);
                } catch (ex: Exception) {

                }
            }
        }
    }
}