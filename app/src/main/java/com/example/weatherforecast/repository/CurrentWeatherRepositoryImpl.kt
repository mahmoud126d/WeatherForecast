package com.example.weatherforecast.repository

import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.FiveDaysWeatherResponse
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow

class CurrentWeatherRepositoryImpl private constructor(
    private val currentWeatherRemoteRepository: CurrentWeatherRemoteDataSource
) : CurrentWeatherRepository {
    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        appId: String
    ): Flow<CurrentWeatherResponse> {
        return currentWeatherRemoteRepository.getCurrentWeather(lat, lon, appId)
    }

    override suspend fun getFiveDaysWeather(
        lat: Double,
        lon: Double,
        appId: String
    ): Flow<FiveDaysWeatherResponse> {
        return currentWeatherRemoteRepository.getFiveDaysWeather(lat, lon, appId)
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