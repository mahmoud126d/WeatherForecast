package com.example.weatherforecast.model

import com.google.gson.annotations.SerializedName


data class FiveDaysWeatherResponse(
    val list: List<CurrentWeatherResponse>
)


data class CurrentWeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String,
    val wind: WindSpeed,
    val clouds: Cloud,
    @SerializedName("dt_txt")
    val date:String
)

data class Main(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class Weather(
    val description: String,
    val icon:String
)

data class WindSpeed(
    val speed: Double
)

data class Cloud(
    val all: Int
)
data class DayWeather(
    val temp:Double,
    val icon:String,
    val time:String
)
fun CurrentWeatherResponse.toCurrentWeather(): CurrentWeather {
    return CurrentWeather(
        temperature = main.temp,
        humidity = main.humidity,
        description = weather.firstOrNull()?.description ?: "No description",
        cloud = clouds.all,
        pressure = main.pressure,
        city = name,
        speed = wind.speed,
        icon = weather.firstOrNull()?.icon ?: "No icon"
    )
}

fun FiveDaysWeatherResponse.toFiveDaysWeather(): List<CurrentWeather> {
    val list = mutableListOf<CurrentWeather>()
    for (current in this.list) {
        list.add(
            CurrentWeather(
                temperature = current.main.temp,
                humidity = current.main.humidity,
                description = current.weather.firstOrNull()?.description ?: "No description",
                cloud = current.clouds.all,
                pressure = current.main.pressure,
                speed = current.wind.speed,
                date = current.date,
                icon = current.weather.firstOrNull()?.icon ?: "No icon"
            )
        )
    }
    return list
}