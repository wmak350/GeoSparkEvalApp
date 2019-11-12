package com.wilmak.geosparkapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface GeoSparkAPIService {
    @Headers("Content-Type: application/json", "Api-Key: cfde45263a0e40daaeb25f3fcad8ba0e")
    @POST("v1/api/trips/")
    fun CreateTrip(@Body requestUserId: CreateTripModel.RequestUserId): Call<CreateTripModel.ResponseData>
}
