package com.wilmak.geosparkapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.geospark.lib.GeoSpark
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.*

class GeoSparkFBMessagingService : FirebaseMessagingService() {

    companion object {
        public val GEOSPARK_NOTIFICATION_ID_STR = "GSNotificationId"
        public val GEOSPARK_NOTIFICATION_NAME_STR = "GSNotificationName"
    }

    //This code will update the Device token
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        token.let {
            val preferences = getSharedPreferences("GeoSparkDemoApp", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString("deviceToken", token)
            editor.commit()
            Log.i("GSFBMessagingService", "Device Token: ${token}")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        showNotification(applicationContext, remoteMessage)
    }

    private fun showNotification(context: Context, remoteMessage: RemoteMessage) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    GEOSPARK_NOTIFICATION_ID_STR,
                    GEOSPARK_NOTIFICATION_NAME_STR,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(GeoSpark.EXTRA, GeoSpark.notificationReceiveHandler(remoteMessage.data))
            val pendingIntent = TaskStackBuilder.create(this).run {
                addNextIntentWithParentStack(intent)
                // Get the PendingIntent containing the entire back stack
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            val builder = NotificationCompat.Builder(context, GEOSPARK_NOTIFICATION_ID_STR)
            if (remoteMessage.data["message"]!!.length > 50) {
                builder.setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.ic_launcher
                        )
                    )
                    .setContentTitle("Campaign")
                    .setStyle(NotificationCompat.BigTextStyle().bigText(remoteMessage.data["message"]))
                    .setContentIntent(pendingIntent)
            } else {
                builder.setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.ic_launcher
                        )
                    )
                    .setContentTitle("Campaign")
                    .setContentText(remoteMessage.data["message"])
                    .setContentIntent(pendingIntent)
            }
            builder.setAutoCancel(true)
            notificationManager.notify(getId(), builder.build())
        } catch (e: Exception) {
        }
    }

    private fun getId(): Int {
        return Random().nextInt(100000)
    }
}
