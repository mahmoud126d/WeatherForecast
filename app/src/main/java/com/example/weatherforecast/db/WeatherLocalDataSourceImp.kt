package com.example.weatherforecast.db

import com.example.weatherforecast.model.AlertData
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

    override suspend fun getWeather(cityName: String): Flow<CurrentWeather?> {
        return dao.getWeatherByCity(cityName)
    }

    override suspend fun insertAlert(alert: AlertData): Long {
        return dao.insertAlert(alert)
    }

    override suspend fun getAllAlerts(): Flow<List<AlertData>>? {
        return dao.getAllAlerts()
    }

    override suspend fun deleteAlert(alert: AlertData): Int {
        return dao.deleteAlert(alert)
    }

    override suspend fun deleteOldAlerts(currentTime: Long): Int {
        return dao.deleteOldAlerts(currentTime)
    }
}