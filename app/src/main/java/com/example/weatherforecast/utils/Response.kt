package com.example.weatherforecast.utils

import com.example.weatherforecast.model.WeatherData

sealed class Response {
    data object Loading : Response()
    data class Success(val data: WeatherData) : Response()
    data class Failure(val error: Throwable) : Response()
}