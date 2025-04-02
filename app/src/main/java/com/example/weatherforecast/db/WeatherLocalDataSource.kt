package com.example.weatherforecast.db

import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.model.WeatherData
import com.example.weatherforecast.model.HomeWeather
import kotlinx.coroutines.flow.Flow

interface WeatherLocalDataSource {
    suspend fun insertWeather(weather: WeatherData):Long
    suspend fun deleteWeather(weather: WeatherData):Int
    suspend fun getAllWeather(): Flow<List<WeatherData>>?
    suspend fun getWeather(lon: Double, lat: Double): Flow<WeatherData?>
    suspend fun insertAlert(alert:AlertData):Long
    suspend fun getAllAlerts(): Flow<List<AlertData>>?
    suspend fun deleteAlert(date: String, time: String):Int
    suspend fun deleteOldAlerts(currentTime: Long):Int
    suspend fun insertHomeWeather(weather: HomeWeather):Long
    suspend fun getHomeWeather():Flow<HomeWeather?>
    suspend fun getWorkId(date: String, time: String): String?
}