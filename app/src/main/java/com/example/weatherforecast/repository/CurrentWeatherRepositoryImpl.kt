package com.example.weatherforecast.repository

import com.example.weatherforecast.db.WeatherLocalDataSource
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.FiveDaysWeatherResponse
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow

class CurrentWeatherRepositoryImpl private constructor(
    private val currentWeatherRemoteRepository: CurrentWeatherRemoteDataSource,
    private val weatherLocalDataSource: WeatherLocalDataSource
) : CurrentWeatherRepository {
    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        unit:String,
        lang:String,
        appId:String,
    ): Flow<CurrentWeatherResponse> {
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
    ): Flow<FiveDaysWeatherResponse> {
        return currentWeatherRemoteRepository.getFiveDaysWeather(
            lat ,
            lon ,
            unit,
            lang,
            appId,
        )
    }

    override suspend fun insertWeather(weather: CurrentWeather) = weatherLocalDataSource.insertWeather(weather)

    override suspend fun deleteWeather(weather: CurrentWeather) = weatherLocalDataSource.deleteWeather(weather)

    override suspend fun getAllWeather(): Flow<List<CurrentWeather>>? = weatherLocalDataSource.getAllWeather()

    companion object {
        private var INSTANCE: CurrentWeatherRepositoryImpl? = null
        fun getInstance(
            currentWeatherRemoteRepository: CurrentWeatherRemoteDataSource,
            weatherLocalDataSource: WeatherLocalDataSource
        ): CurrentWeatherRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                val temp = CurrentWeatherRepositoryImpl(currentWeatherRemoteRepository,weatherLocalDataSource)
                INSTANCE = temp
                temp
            }
        }
    }

}