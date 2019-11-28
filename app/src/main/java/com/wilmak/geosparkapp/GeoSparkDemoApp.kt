package com.wilmak.geosparkapp

import android.app.Application
import android.content.Intent
import android.util.Log
import com.geospark.lib.GeoSpark
import com.geospark.lib.callback.GeoSparkCallBack
import com.geospark.lib.model.GeoSparkError
import com.geospark.lib.model.GeoSparkUser

class GeoSparkDemoApp : Application() {

    companion object {
        val ACTION_DEMOAPP_LOCATION_INFO = "android.intent.action.geospark_locinfo"
        val ACTION_DEMOAPP_USER_LOGIN = "android.intent.action.geospark_demoapp.user_created"
        val ACTION_DEMOAPP_PERIODIC_LOCATION_UPDATE = "android.intent.action.geospark_demoapp.periodic_loca_update"
        val ACTION_DEMOAPP_USER_LOGIN_FAILURE = "android.intent.action.geospark_demoapp.user_creation_failure"

        val UserIdMap = mapOf(
            "wilmak" to "5dbe27bf969406161a5ea32d",
            "demo1" to "5dbabd099694060a045e8f3b",
            "demo2" to "5db95ac5e47bae38249fbe6c",
            "user1" to "5db9544ae47bae38249fbe1e"
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Publisher Key: 43a20d3e800403f398f001e12564aaf9c1b13b80f471fdad2d42f541c030e050
        // API Key: cfde45263a0e40daaeb25f3fcad8ba0e
        GeoSpark.initialize(this, "43a20d3e800403f398f001e12564aaf9c1b13b80f471fdad2d42f541c030e050")

    }
}