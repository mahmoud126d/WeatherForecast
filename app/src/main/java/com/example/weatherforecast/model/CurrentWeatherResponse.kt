package com.example.weatherforecast.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class FiveDaysWeatherResponse(
    val list: List<CurrentWeatherResponse>
)


data class CurrentWeatherResponse(
    val coord: Coord,
    val main: Main,
    val weather: List<Weather>,
    val name: String,
    val wind: WindSpeed,
    val clouds: Cloud,
    @SerializedName("dt_txt")
    val date:String,
    val sys:Sys
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
data class Coord(
    val lon:Double,
    val lat:Double
)
data class Sys(
    val country:String
)
data class DayWeather(
    val temp:Double,
    val icon:String,
    var time:String
)
lateinit var currentWeather: CurrentWeather
@RequiresApi(Build.VERSION_CODES.O)
fun CurrentWeatherResponse.toCurrentWeather(): CurrentWeather {
    currentWeather = CurrentWeather(
        lat = coord.lat,
        lon = coord.lon,
        temperature = main.temp,
        humidity = main.humidity,
        description = weather.firstOrNull()?.description ?: "No description",
        cloud = clouds.all,
        pressure = main.pressure,
        city = name,
        speed = wind.speed,
        icon = weather.firstOrNull()?.icon ?: "No icon",
        country = sys.country
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


@RequiresApi(Build.VERSION_CODES.O)
fun CurrentWeather.toHomeWeather(): HomeWeather {
    return  HomeWeather(
        temperature = temperature,
        humidity = humidity,
        description = description,
        cloud = cloud,
        pressure = pressure,
        city = city,
        speed = speed,
        icon = icon,
        lastUpdate = lastUpdate,
        listOfDayWeather = listOfDayWeather,
        listOfHourlyWeather = listOfHourlyWeather
    )
}
@RequiresApi(Build.VERSION_CODES.O)
fun HomeWeather.toCurrentWeather(): CurrentWeather {
    return  CurrentWeather(
        temperature = temperature,
        humidity = humidity,
        description = description,
        cloud = cloud,
        pressure = pressure,
        city = city,
        speed = speed,
        icon = icon,
        lastUpdate = lastUpdate,
        listOfDayWeather = listOfDayWeather,
        listOfHourlyWeather = listOfHourlyWeather
    )
}