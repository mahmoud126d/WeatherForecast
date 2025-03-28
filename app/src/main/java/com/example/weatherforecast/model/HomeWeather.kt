package com.example.weatherforecast.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "home_weather")
@TypeConverters(WeatherTypeConverters::class)
data class HomeWeather(
    val temperature: Double=0.0,
    val humidity: Int = 0,
    val description: String = "",
    val pressure:Int =0,
    @PrimaryKey val city:String = "Default City",
    val speed:Double= 0.0,
    val cloud:Int= 0,
    val date:String= "Default Date",
    val icon:String= "Default Date",
    var address:String = "",
    var listOfHourlyWeather:List<DayWeather> = emptyList(),
    var listOfDayWeather:List<DayWeather> = emptyList(),
    val lastUpdate:String = ""
)