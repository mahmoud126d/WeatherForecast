package com.example.weatherforecast.repository

import com.example.weatherforecast.model.CurrentWeatherResponse

interface CurrentWeatherRepository {
    suspend fun getCurrentWeather(lat:Double,lon:Double,appId:String): CurrentWeatherResponse?
}