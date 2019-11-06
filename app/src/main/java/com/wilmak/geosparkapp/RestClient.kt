package com.wilmak.geosparkapp

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

object RestClient {
    const val BASE_URL = "https://api.geospark.co"
    private var mClient: Retrofit? = null

    val client: Retrofit
        get() {
            if (mClient == null) {
                mClient = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return mClient!!
        }
}

