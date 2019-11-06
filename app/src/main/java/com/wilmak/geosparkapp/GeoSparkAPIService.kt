package com.wilmak.geosparkapp

import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface GeoSparkAPIService {
    @Headers("Content-Type: text/json")
    @POST("v1/api/trips/")
    fun CreateTrip(@Header("Api-Key") key: String): Call<CreateTripModel.ResponseData>
}
