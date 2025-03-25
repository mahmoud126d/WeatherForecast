package com.example.weatherforecast.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.model.CurrentWeather
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: CurrentWeather):Long

    @Delete
    suspend fun deleteWeather(weather: CurrentWeather):Int

    @Query("SELECT * FROM current_weather")
    fun getAllWeather():Flow<List<CurrentWeather>>
}