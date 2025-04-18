package com.example.weatherforecast.model

import androidx.room.Entity
import androidx.room.TypeConverters


@Entity(primaryKeys = ["lon", "lat"])
@TypeConverters(WeatherTypeConverters::class)
data class WeatherData(
    val id: Int =0,
    val lon:Double = 0.0,
    val lat:Double=0.0,
    val temperature: Double=0.0,
    val humidity: Int = 0,
    val description: String = "",
    val pressure:Int =0,
    val city:String = "Default City",
    val speed:Double= 0.0,
    val cloud:Int= 0,
    val date:String= "Default Date",
    val icon:String= "Default Date",
    var address:String = "",
    var country:String? ="",
    var listOfHourlyWeather:List<DayWeather> = emptyList(),
    var listOfDayWeather:List<DayWeather> = emptyList(),
    var lastUpdate:String = ""
)
