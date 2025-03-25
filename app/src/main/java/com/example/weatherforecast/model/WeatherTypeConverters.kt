package com.example.weatherforecast.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object WeatherTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromDayWeatherList(list: List<DayWeather>?): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toDayWeatherList(json: String): List<DayWeather> {
        val type = object : TypeToken<List<DayWeather>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}