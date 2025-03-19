package com.example.weatherforecast.network

import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.FiveDaysWeatherResponse

class CurrentWeatherRemoteDataSourceImpl(
    private val service: CurrentWeatherApiService
) : CurrentWeatherRemoteDataSource {
    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        appId: String
    ): CurrentWeatherResponse {
        return service.getCurrentWeather(lat, lon, appId)
    }

    override suspend fun getFiveDaysWeather(
        lat: Double,
        lon: Double,
        appId: String
    ): FiveDaysWeatherResponse {
        return service.getFiveDaysWeather(lat,lon,appId)
    }

}