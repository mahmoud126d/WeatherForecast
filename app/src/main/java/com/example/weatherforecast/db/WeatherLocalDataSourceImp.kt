package com.example.weatherforecast.db

import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.model.WeatherData
import com.example.weatherforecast.model.HomeWeather
import kotlinx.coroutines.flow.Flow

class WeatherLocalDataSourceImp(private val dao:WeatherDao):WeatherLocalDataSource {
    override suspend fun insertWeather(weather: WeatherData): Long {
        return dao.insertWeather(weather)
    }

    override suspend fun deleteWeather(weather: WeatherData): Int {
        return dao.deleteWeather(weather)
    }

    override suspend fun getAllWeather(): Flow<List<WeatherData>> {
        return dao.getAllWeather()
    }

    override suspend fun getWeather(lon: Double, lat: Double): Flow<WeatherData?> {
        return dao.getWeatherLatLon(lon, lat)
    }

    override suspend fun insertAlert(alert: AlertData): Long {
        return dao.insertAlert(alert)
    }

    override suspend fun getAllAlerts(): Flow<List<AlertData>>? {
        return dao.getAllAlerts()
    }

    override suspend fun deleteAlert(date: String, time: String): Int {
        return dao.deleteAlert(date,time)
    }


    override suspend fun deleteOldAlerts(): Int {
        return dao.deleteOldAlerts()
    }

    override suspend fun insertHomeWeather(weather: HomeWeather): Long {
        return dao.insertHomeWeather(weather)
    }

    override suspend fun getHomeWeather(): Flow<HomeWeather?> {
        return dao.getHomeWeather()
    }

    override suspend fun getWorkId(date: String, time: String): String? {
        return dao.getWorkId(date,time)
    }
}