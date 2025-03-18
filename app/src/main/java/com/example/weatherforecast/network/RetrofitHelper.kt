package com.example.weatherforecast.network

import com.example.weatherforecast.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    private const val BASE_URL = Constants.BASE_URL
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitService = retrofit.create(CurrentWeatherApiService::class.java)
}