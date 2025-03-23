package com.example.weatherforecast.network

import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.FiveDaysWeatherResponse
import kotlinx.coroutines.flow.Flow

interface CurrentWeatherRemoteDataSource {
    suspend fun getCurrentWeather(lat: Double, lon: Double, appId: String): Flow<CurrentWeatherResponse>
    suspend fun getFiveDaysWeather(lat: Double, lon: Double, appId: String): Flow<FiveDaysWeatherResponse>
}