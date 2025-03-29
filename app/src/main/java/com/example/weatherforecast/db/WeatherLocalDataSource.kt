package com.example.weatherforecast.db

import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.model.HomeWeather
import kotlinx.coroutines.flow.Flow

interface WeatherLocalDataSource {
    suspend fun insertWeather(weather: CurrentWeather):Long
    suspend fun deleteWeather(weather: CurrentWeather):Int
    suspend fun getAllWeather(): Flow<List<CurrentWeather>>?
    suspend fun getWeather(lon: Double, lat: Double): Flow<CurrentWeather?>
    suspend fun insertAlert(alert:AlertData):Long
    suspend fun getAllAlerts(): Flow<List<AlertData>>?
    suspend fun deleteAlert(alert: AlertData):Int
    suspend fun deleteOldAlerts(currentTime: Long):Int
    suspend fun insertHomeWeather(weather: HomeWeather):Long
    suspend fun getHomeWeather():Flow<HomeWeather?>
}