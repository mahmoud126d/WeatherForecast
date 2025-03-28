package com.example.weatherforecast.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.model.HomeWeather

@Database(entities = arrayOf(CurrentWeather::class, AlertData::class, HomeWeather::class), version = 4)
abstract class WeatherDataBase :RoomDatabase(){
    abstract fun getWeatherDao() : WeatherDao
    companion object{
        @Volatile
        private var INSTANCE:WeatherDataBase? = null
        fun getInstance(context: Context):WeatherDataBase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext, WeatherDataBase::class.java, "weather_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}