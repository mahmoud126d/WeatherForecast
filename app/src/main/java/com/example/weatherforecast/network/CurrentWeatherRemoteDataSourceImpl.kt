package com.example.weatherforecast.network

import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.FiveDaysWeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CurrentWeatherRemoteDataSourceImpl(
    private val service: CurrentWeatherApiService
) : CurrentWeatherRemoteDataSource {
    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        appId: String
    ): Flow<CurrentWeatherResponse> = flow {
        val response = service.getCurrentWeather(lat,lon, appId).body()
        if(response!=null){
            emit(response)
        }else{
            throw Exception("No data Received")
        }
    }

    override suspend fun getFiveDaysWeather(
        lat: Double,
        lon: Double,
        appId: String
    ): Flow<FiveDaysWeatherResponse> = flow{
        val response = service.getFiveDaysWeather(lat,lon, appId).body()
        if(response!=null){
            emit(response)
        }else{
            throw Exception("No data Received")
        }
    }

}