package com.example.weatherforecast.repository

import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.FiveDaysWeatherResponse

interface CurrentWeatherRepository {
    suspend fun getCurrentWeather(lat:Double,lon:Double,appId:String): CurrentWeatherResponse?
    suspend fun getFiveDaysWeather(lat:Double,lon:Double,appId:String):FiveDaysWeatherResponse?
}