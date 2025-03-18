package com.example.weatherforecast.network

import com.example.weatherforecast.model.CurrentWeatherResponse

interface CurrentWeatherRemoteDataSource {
    suspend fun getCurrentWeather(lat:Double,lon:Double,appId:String): CurrentWeatherResponse?
}