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
        val ACTION_DEMOAPP_USER_CREATED = "android.intent.action.geospark_demoapp.user_created"
        val ACTION_DEMOAPP_PERIODIC_LOCATION_UPDATE = "android.intent.action.geospark_demoapp.periodic_loca_update"
        val ACTION_DEMOAPP_USER_CREATION_FAILURE = "android.intent.action.geospark_demoapp.user_creation_failure"
    }

    override fun onCreate() {
        super.onCreate()
        // Publisher Key: 43a20d3e800403f398f001e12564aaf9c1b13b80f471fdad2d42f541c030e050
        // API Key: cfde45263a0e40daaeb25f3fcad8ba0e
        GeoSpark.initialize(this, "43a20d3e800403f398f001e12564aaf9c1b13b80f471fdad2d42f541c030e050")

    }
}