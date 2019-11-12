package com.wilmak.geosparkapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.geospark.lib.GeoSpark
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class GeoSparkDemoForegroundService : Service() {

    private lateinit var mExecutor: ScheduledThreadPoolExecutor
    private lateinit var mWakeupLock: PowerManager.WakeLock

    companion object {
        const val GEOSPARK_EVALAPP_START_PERIODIC_UPDATE = "GEOSPARK_EVALAPP.action.start_periodic_loc_updates"
        const val GEOSPARK_EVALAPP_STOP_PERIODIC_UPDATE = "GEOSPARK_EVALAPP.action.stop_periodic_loc_updates"
        val CHANNEL_ID = "ForegroundServiceChannel"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null)
            return START_STICKY
        acquireWakeupLock()
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Foreground Periodic Location Updates starting...")
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        when (intent.action) {
            GEOSPARK_EVALAPP_START_PERIODIC_UPDATE -> startPeriodicLocationUpdates()
            GEOSPARK_EVALAPP_STOP_PERIODIC_UPDATE -> stopPeriodicLocationUpdates()
        }
        return START_STICKY
    }

    private fun acquireWakeupLock() {
        val pwrMgr = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeupLock = pwrMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GeoSparkDemoApp:")
        mWakeupLock.acquire()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        mWakeupLock.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startPeriodicLocationUpdates() {
        mExecutor = ScheduledThreadPoolExecutor(5)
        mExecutor.scheduleAtFixedRate(
            object: Runnable {
                override fun run() {
                    val intent = Intent(GeoSparkDemoApp.ACTION_DEMOAPP_PERIODIC_LOCATION_UPDATE)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    sendBroadcast(intent)
                }
            }, 0, 45, TimeUnit.SECONDS)
    }

    private fun stopPeriodicLocationUpdates() {
        mExecutor.shutdown()
    }
}
