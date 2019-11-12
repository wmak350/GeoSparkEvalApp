package com.wilmak.geosparkapp

import com.google.android.gms.maps.model.LatLng

enum class DistanceUnit {
    Miles, Kilometers
}

fun getHaversineDistance(pos1: LatLng, pos2: LatLng, unit: DistanceUnit): Double {
    val R = (if (unit === DistanceUnit.Miles) 3960 else 6371).toDouble()
    val lat = Math.toRadians(pos2.latitude - pos1.latitude)
    val lng = Math.toRadians(pos2.longitude - pos1.longitude)
    val h1 =
        Math.sin(lat / 2) * Math.sin(lat / 2) +
                Math.cos(Math.toRadians(pos1.latitude)) * Math.cos(Math.toRadians(pos2.latitude)) *
                Math.sin(lng / 2) * Math.sin(lng / 2)
    val h2 = 2 * Math.asin(Math.min(1.0, Math.sqrt(h1)))
    return R * h2
}
