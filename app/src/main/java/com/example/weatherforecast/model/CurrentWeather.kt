package com.example.weatherforecast.model

data class CurrentWeather(
    val temperature: Double,
    val humidity: Int,
    val description: String,
    val pressure:Int,
    val city:String = "Default City",
    val speed:Double,
    val cloud:Int,
    val date:String= "Default Date",
    val icon:String
)
