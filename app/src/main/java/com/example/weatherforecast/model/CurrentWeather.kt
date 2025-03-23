package com.example.weatherforecast.model

data class CurrentWeather(
    val temperature: Double=0.0,
    val humidity: Int = 0,
    val description: String = "",
    val pressure:Int =0,
    val city:String = "Default City",
    val speed:Double= 0.0,
    val cloud:Int= 0,
    val date:String= "Default Date",
    val icon:String= "Default Date",
    var listOfHourlyWeather:List<DayWeather> = emptyList(),
    var listOfDayWeather:List<DayWeather> = emptyList()
)
