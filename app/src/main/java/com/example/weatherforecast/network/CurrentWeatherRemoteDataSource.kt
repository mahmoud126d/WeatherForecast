package com.example.weatherforecast.network

import com.example.weatherforecast.model.WeatherResponse
import com.example.weatherforecast.model.ForecastWeatherResponse
import kotlinx.coroutines.flow.Flow

interface CurrentWeatherRemoteDataSource {
    suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        unit:String,
        lang:String,
        appId:String,
    ): Flow<WeatherResponse>
    suspend fun getFiveDaysWeather(
        lat: Double,
        lon: Double,
        unit:String,
        lang:String,
        appId:String,
    ): Flow<ForecastWeatherResponse>
}