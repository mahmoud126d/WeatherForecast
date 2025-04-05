package com.example.weatherforecast.repository

import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.model.WeatherData
import com.example.weatherforecast.model.WeatherResponse
import com.example.weatherforecast.model.ForecastWeatherResponse
import com.example.weatherforecast.model.HomeWeather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
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
    ):Flow<ForecastWeatherResponse>
    suspend fun insertWeather(weather: WeatherData):Long
    suspend fun deleteWeather(weather: WeatherData):Int
    suspend fun getAllWeather(): Flow<List<WeatherData>>?
    suspend fun getWeather(lon: Double, lat: Double): Flow<WeatherData?>
    suspend fun insertAlert(alertData: AlertData):Long
    suspend fun getAllAlerts(): Flow<List<AlertData>>?
    suspend fun deleteAlert(date: String, time: String):Int
    suspend fun deleteOldAlerts():Int
    suspend fun insertHomeWeather(weather: HomeWeather):Long
    suspend fun getHomeWeather():Flow<HomeWeather?>
    suspend fun getWorkId(date: String, time: String): String?
    suspend fun getWeatherLatLon(longitude: Double, latitude: Double):Flow<WeatherData?>
}