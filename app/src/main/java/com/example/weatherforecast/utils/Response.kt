package com.example.weatherforecast.utils

import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.model.CurrentWeatherResponse

sealed class Response {
    data object Loading : Response()
    data class Success(val data: CurrentWeather) : Response()
    data class Failure(val error: Throwable) : Response()
}