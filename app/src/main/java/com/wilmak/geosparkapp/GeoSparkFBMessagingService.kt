package com.wilmak.geosparkapp

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.geospark.lib.GeoSpark
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

class GeoSparkFBMessagingService : FirebaseMessagingService() {

    companion object {
        public val GEOSPARK_NOTIFICATION_ID_STR = "GeoSparkNotificationId"
        public val GEOSPARK_NOTIFICATION_ID = 0
    }

    //This code will update the Device token
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        token.let {
            val preferences = getSharedPreferences("GeoSparkDemoApp", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString("deviceToken", token)
            editor.commit()
            Log.i("GSFBMessagingService" , "Device Token: ${token}")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(GeoSpark.EXTRA, GeoSpark.notificationReceiveHandler(remoteMessage.getData()));
        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val builder = NotificationCompat.Builder(this, GEOSPARK_NOTIFICATION_ID_STR)
            .apply {
                setAutoCancel(true)
                setContentIntent(pendingIntent)
            }
        notificationManager.notify(GEOSPARK_NOTIFICATION_ID, builder.build());
    }
}
