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
    var time:String
)
lateinit var currentWeather: CurrentWeather
fun CurrentWeatherResponse.toCurrentWeather(): CurrentWeather {
    currentWeather = CurrentWeather(
        temperature = main.temp,
        humidity = main.humidity,
        description = weather.firstOrNull()?.description ?: "No description",
        cloud = clouds.all,
        pressure = main.pressure,
        city = name,
        speed = wind.speed,
        icon = weather.firstOrNull()?.icon ?: "No icon",
    )
    return currentWeather
}

fun FiveDaysWeatherResponse.toFiveDaysWeather(): CurrentWeather {
    val list = mutableListOf<DayWeather>()
    for (current in this.list) {
        list.add(
            DayWeather(
                temp = current.main.temp,
                icon = current.weather.firstOrNull()?.icon ?: "",
                time = current.date
            )
        )
    }
    currentWeather.listOfDayWeather = list
    return currentWeather
}
fun FiveDaysWeatherResponse.toHourlyWeather(): CurrentWeather {
    val list = mutableListOf<DayWeather>()
    for (current in this.list) {
        list.add(
            DayWeather(
                temp = current.main.temp,
                icon = current.weather.firstOrNull()?.icon ?: "",
                time = current.date
            )
        )
    }
    currentWeather.listOfHourlyWeather = list
    return currentWeather
}