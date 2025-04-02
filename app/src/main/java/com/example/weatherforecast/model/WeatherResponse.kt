package com.example.weatherforecast.model

import com.google.gson.annotations.SerializedName


data class ForecastWeatherResponse(
    val list: List<WeatherResponse>
)


data class WeatherResponse(
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
lateinit var weatherData: WeatherData
fun WeatherResponse.toCurrentWeather(): WeatherData {
    weatherData = WeatherData(
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
    return weatherData
}

fun ForecastWeatherResponse.toFiveDaysWeather(): WeatherData {
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
    weatherData.listOfDayWeather = list
    return weatherData
}
fun ForecastWeatherResponse.toHourlyWeather(): WeatherData {
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
    weatherData.listOfHourlyWeather = list
    return weatherData
}


fun WeatherData.toHomeWeather(): HomeWeather {
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
fun HomeWeather.toCurrentWeather(): WeatherData {
    return  WeatherData(
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