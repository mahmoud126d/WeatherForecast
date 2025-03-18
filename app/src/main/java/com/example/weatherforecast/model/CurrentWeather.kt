package com.example.weatherforecast.model

data class CurrentWeather(
    val temperature: Double,
    val humidity: Int,
    val description: String,
    val pressure:Int,
    val city:String,
    val speed:Double,
    val cloud:Int
)
