package com.wilmak.geosparkapp

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import com.geospark.lib.model.GeoSparkError
import com.geospark.lib.model.GeoSparkUser
import com.geospark.lib.location.services.GeoSparkReceiver

class GeoSparkDemoReceiver : GeoSparkReceiver() {
    override fun onLocationUpdated(
        context: Context, location: Location,
        geoSparkUser: GeoSparkUser, activity: String
    ) {
        val bundle = createLocationInfoBundle(location)
        val intent = Intent(GeoSparkDemoApp.ACTION_DEMOAPP_LOCATION_INFO)
        intent.putExtra("locationInfo", bundle)
        context.sendBroadcast(intent)
    }

    override fun onError(context: Context, geoSparkError: GeoSparkError) {
        geoSparkError.errorCode
        geoSparkError.errorMessage
    }

    private fun createLocationInfoBundle(location: Location): Bundle {
        val locationInfo = LocationInfo()
        with(locationInfo) {
            if (location.hasAccuracy())
                accuracy = location.accuracy.toDouble()
            if (location.hasBearing())
                bearing = location.bearing.toDouble()
            if (location.hasSpeed())
                speed = location.speed.toDouble()
            latitude = location.latitude
            longitude = location.longitude
            altitude = location.altitude.toDouble()
            elpasedTime = location.elapsedRealtimeNanos.toDouble()
            time = location.time
            provider = location.provider
        }
        val bundle = Bundle()
        bundle.putSerializable("Geospark.LocationInfo", locationInfo)
        return bundle
    }
}