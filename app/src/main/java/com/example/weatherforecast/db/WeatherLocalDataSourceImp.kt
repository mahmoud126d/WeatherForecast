package com.example.weatherforecast.db

import com.example.weatherforecast.model.CurrentWeather
import kotlinx.coroutines.flow.Flow

class WeatherLocalDataSourceImp(private val dao:WeatherDao):WeatherLocalDataSource {
    override suspend fun insertWeather(weather: CurrentWeather): Long {
        return dao.insertWeather(weather)
    }

    override suspend fun deleteWeather(weather: CurrentWeather): Int {
        return dao.deleteWeather(weather)
    }

    override suspend fun getAllWeather(): Flow<List<CurrentWeather>> {
        return dao.getAllWeather()
    }
}