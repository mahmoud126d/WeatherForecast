package com.example.weatherforecast.network
import com.example.weatherforecast.model.WeatherResponse
import com.example.weatherforecast.model.ForecastWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrentWeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("units") unit:String,
        @Query("lang") lang:String,
        @Query("appid") appId:String,

    ): Response<WeatherResponse>
    @GET("data/2.5/forecast")
    suspend fun getFiveDaysWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("units") unit:String,
        @Query("lang") lang:String,
        @Query("appid") appId:String,
    ):Response<ForecastWeatherResponse>

}