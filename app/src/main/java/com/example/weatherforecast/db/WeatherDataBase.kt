package com.example.weatherforecast.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherforecast.model.CurrentWeather

@Database(entities = arrayOf(CurrentWeather::class), version = 1)
abstract class WeatherDataBase :RoomDatabase(){
    abstract fun getWeatherDao() : WeatherDao
    companion object{
        @Volatile
        private var INSTANCE:WeatherDataBase? = null
        fun getInstance(context: Context):WeatherDataBase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext, WeatherDataBase::class.java, "weather_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}