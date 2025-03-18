package com.example.weatherforecast.repository

import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSource

class CurrentWeatherRepositoryImpl private constructor(
    private val currentWeatherRemoteRepository: CurrentWeatherRemoteDataSource
) : CurrentWeatherRepository {
    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        appId: String
    ): CurrentWeatherResponse? {
        return currentWeatherRemoteRepository.getCurrentWeather(lat, lon, appId)
    }

    companion object {
        private var INSTANCE: CurrentWeatherRepositoryImpl? = null
        fun getInstance(
            currentWeatherRemoteRepository: CurrentWeatherRemoteDataSource
        ): CurrentWeatherRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                val temp = CurrentWeatherRepositoryImpl(currentWeatherRemoteRepository)
                INSTANCE = temp
                temp
            }
        }
    }

}