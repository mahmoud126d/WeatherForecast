package com.example.weatherforecast.model

data class CurrentWeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String,
    val wind: WindSpeed,
    val clouds: Cloud
)

data class Main(
    val temp: Double,
    val humidity: Int,
    val pressure :Int
)

data class Weather(
    val description: String
)
data class WindSpeed(
    val speed:Double
)
data class Cloud(
    val all:Int
)
fun CurrentWeatherResponse.toCurrentWeather(): CurrentWeather {
    return CurrentWeather(
        temperature = this.main.temp,
        humidity = this.main.humidity,
        description = this.weather.firstOrNull()?.description ?: "No description",
        cloud = this.clouds.all,
        pressure = this.main.pressure,
        city = this.name,
        speed = this.wind.speed
    )
}