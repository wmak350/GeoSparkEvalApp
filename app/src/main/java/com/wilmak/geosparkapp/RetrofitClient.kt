package com.wilmak.geosparkapp

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val BASE_URL = "https://api.geospark.co"
    private var mClient: Retrofit? = null

    val client: Retrofit
        get() {
            if (mClient == null) {

                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                val okClient = OkHttpClient.Builder()
                    .connectTimeout(300, TimeUnit.SECONDS)
                    .readTimeout(300, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build()
                mClient = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return mClient!!
        }
}

interface GeoSparkDemoAppNetCallback<T> {
    fun onSuccess(data: T)
    fun onFailure(t: Throwable?, msg: String?)
}

fun createTrip(userId: String, callback: GeoSparkDemoAppNetCallback<CreateTripModel.ResponseData>) {
    val client = RetrofitClient.client
    val svc = client.create(GeoSparkAPIService::class.java)
    val call = svc.CreateTrip(CreateTripModel.RequestUserId(userId))
    call.enqueue(object: Callback<CreateTripModel.ResponseData> {
        override fun onFailure(call: Call<CreateTripModel.ResponseData>, t: Throwable) {
            callback.onFailure(t, null)
        }

        override fun onResponse(
            call: Call<CreateTripModel.ResponseData>,
            response: Response<CreateTripModel.ResponseData>
        ) {
            response.body()?.let {
                callback.onSuccess(it)
                return
            }
            callback.onFailure(null, "Empty returned body")
        }
    })
}
