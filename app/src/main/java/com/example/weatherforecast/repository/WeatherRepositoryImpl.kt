package com.example.weatherforecast.repository

import com.example.weatherforecast.db.WeatherLocalDataSource
import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.model.WeatherData
import com.example.weatherforecast.model.WeatherResponse
import com.example.weatherforecast.model.ForecastWeatherResponse
import com.example.weatherforecast.model.HomeWeather
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow

class WeatherRepositoryImpl private constructor(
    private val currentWeatherRemoteRepository: CurrentWeatherRemoteDataSource,
    private val weatherLocalDataSource: WeatherLocalDataSource
) : WeatherRepository {
    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        unit:String,
        lang:String,
        appId:String,
    ): Flow<WeatherResponse> {
        return currentWeatherRemoteRepository.getCurrentWeather(
            lat ,
            lon ,
            unit,
            lang,
            appId,
        )
    }

    override suspend fun getFiveDaysWeather(
        lat: Double,
        lon: Double,
        unit:String,
        lang:String,
        appId:String,
    ): Flow<ForecastWeatherResponse> {
        return currentWeatherRemoteRepository.getFiveDaysWeather(
            lat ,
            lon ,
            unit,
            lang,
            appId,
        )
    }

    override suspend fun insertWeather(weather: WeatherData) = weatherLocalDataSource.insertWeather(weather)

    override suspend fun deleteWeather(weather: WeatherData) = weatherLocalDataSource.deleteWeather(weather)

    override suspend fun getAllWeather(): Flow<List<WeatherData>>? = weatherLocalDataSource.getAllWeather()
    override suspend fun getWeather(lon: Double, lat: Double): Flow<WeatherData?> =weatherLocalDataSource.getWeather(lon,lat)
    override suspend fun insertAlert(alertData: AlertData) = weatherLocalDataSource.insertAlert(alertData)
    override suspend fun getAllAlerts(): Flow<List<AlertData>>? {
        return weatherLocalDataSource.getAllAlerts()
    }

    override suspend fun deleteAlert(date: String, time: String): Int {
        return weatherLocalDataSource.deleteAlert(date,time)
    }


    override suspend fun deleteOldAlerts(): Int {
        return weatherLocalDataSource.deleteOldAlerts()
    }

    override suspend fun insertHomeWeather(weather: HomeWeather): Long {
        return weatherLocalDataSource.insertHomeWeather(weather)
    }

    override suspend fun getHomeWeather(): Flow<HomeWeather?> {
        return weatherLocalDataSource.getHomeWeather()
    }

    override suspend fun getWorkId(date: String, time: String): String? {
        return weatherLocalDataSource.getWorkId(date,time)
    }

    companion object {
        private var INSTANCE: WeatherRepositoryImpl? = null
        fun getInstance(
            currentWeatherRemoteRepository: CurrentWeatherRemoteDataSource,
            weatherLocalDataSource: WeatherLocalDataSource
        ): WeatherRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                val temp = WeatherRepositoryImpl(currentWeatherRemoteRepository,weatherLocalDataSource)
                INSTANCE = temp
                temp
            }
        }
    }

}