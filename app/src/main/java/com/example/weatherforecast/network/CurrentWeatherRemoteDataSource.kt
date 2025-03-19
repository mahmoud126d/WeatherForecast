package com.example.weatherforecast.network

import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.FiveDaysWeatherResponse

interface CurrentWeatherRemoteDataSource {
    suspend fun getCurrentWeather(lat:Double,lon:Double,appId:String): CurrentWeatherResponse?
    suspend fun getFiveDaysWeather(lat:Double,lon:Double,appId:String):FiveDaysWeatherResponse?
}