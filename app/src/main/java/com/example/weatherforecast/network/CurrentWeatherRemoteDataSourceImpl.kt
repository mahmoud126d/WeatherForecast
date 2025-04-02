package com.example.weatherforecast.network

import com.example.weatherforecast.model.WeatherResponse
import com.example.weatherforecast.model.ForecastWeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CurrentWeatherRemoteDataSourceImpl(
    private val service: CurrentWeatherApiService
) : CurrentWeatherRemoteDataSource {
    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        unit:String,
        lang:String,
        appId:String,
    ): Flow<WeatherResponse> = flow {
        val response = service.getCurrentWeather(lat,lon,unit,lang,appId).body()
        if(response!=null){
            emit(response)
        }else{
            throw Exception("No data Received")
        }
    }

    override suspend fun getFiveDaysWeather(
        lat: Double,
        lon: Double,
        unit:String,
        lang:String,
        appId:String,
    ): Flow<ForecastWeatherResponse> = flow{
        val response = service.getFiveDaysWeather(
            lat,
            lon,
            unit,
            lang,
            appId
        ).body()
        if(response!=null){
            emit(response)
        }else{
            throw Exception("No data Received")
        }
    }

}