package com.example.weatherforecast.network

import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.FiveDaysWeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Query

class CurrentWeatherRemoteDataSourceImpl(
    private val service: CurrentWeatherApiService
) : CurrentWeatherRemoteDataSource {
    override suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        unit:String,
        lang:String,
        appId:String,
    ): Flow<CurrentWeatherResponse> = flow {
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
    ): Flow<FiveDaysWeatherResponse> = flow{
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