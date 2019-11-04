package com.wilmak.geosparkapp

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GeoSparkDemoAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {

            val intent = Intent(it, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context?.startActivity(intent)
            isAppRunning(it)
        }
    }

    private fun isAppRunning(ctx: Context): Boolean {
        val actManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        actManager.runningAppProcesses.forEach{
            Log.i("GSDemoAlarmReceiver", it.processName)
        }
        return true
    }
}