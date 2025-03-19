package com.example.weatherforecast.network
import com.example.weatherforecast.model.CurrentWeatherResponse
import com.example.weatherforecast.model.FiveDaysWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrentWeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(@Query("lat") lat:Double,@Query("lon") lon:Double,@Query("appid") appId:String):CurrentWeatherResponse
    @GET("data/2.5/forecast")
    suspend fun getFiveDaysWeather(@Query("lat") lat:Double,@Query("lon") lon:Double,@Query("appid") appId:String):FiveDaysWeatherResponse

}