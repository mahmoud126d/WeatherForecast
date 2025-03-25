package com.example.weatherforecast.db

import com.example.weatherforecast.model.CurrentWeather
import kotlinx.coroutines.flow.Flow

interface WeatherLocalDataSource {
    suspend fun insertWeather(weather: CurrentWeather):Long
    suspend fun deleteWeather(weather: CurrentWeather):Int
    suspend fun getAllWeather(): Flow<List<CurrentWeather>>?
}