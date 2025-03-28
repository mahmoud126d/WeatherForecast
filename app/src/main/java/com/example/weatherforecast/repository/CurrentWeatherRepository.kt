package com.example.weatherforecast.repository

import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.FiveDaysWeatherResponse
import kotlinx.coroutines.flow.Flow

interface CurrentWeatherRepository {
    suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        unit:String,
        lang:String,
        appId:String,
    ): Flow<CurrentWeatherResponse>
    suspend fun getFiveDaysWeather(
        lat: Double,
        lon: Double,
        unit:String,
        lang:String,
        appId:String,
    ):Flow<FiveDaysWeatherResponse>
    suspend fun insertWeather(weather: CurrentWeather):Long
    suspend fun deleteWeather(weather: CurrentWeather):Int
    suspend fun getAllWeather(): Flow<List<CurrentWeather>>?
    suspend fun getWeather(cityName: String): Flow<CurrentWeather?>
    suspend fun insertAlert(alertData: AlertData):Long
    suspend fun getAllAlerts(): Flow<List<AlertData>>?
    suspend fun deleteAlert(alertData: AlertData):Int
    suspend fun deleteOldAlerts(currentTime: Long):Int

}