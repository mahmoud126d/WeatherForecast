package com.example.weatherforecast.network

import com.example.weatherforecast.model.CurrentWeatherResponse

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

}