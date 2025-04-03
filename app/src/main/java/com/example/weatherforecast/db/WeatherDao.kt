package com.example.weatherforecast.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.model.WeatherData
import com.example.weatherforecast.model.HomeWeather
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherData):Long

    @Delete
    suspend fun deleteWeather(weather: WeatherData):Int

    @Query("SELECT * FROM WeatherData")
    fun getAllWeather():Flow<List<WeatherData>>

    @Query("SELECT * FROM WeatherData WHERE lat = :latitude AND lon = :longitude LIMIT 1")
    fun getWeatherLatLon( longitude: Double,latitude: Double): Flow<WeatherData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alertData: AlertData):Long

    @Query("SELECT workId FROM AlertData WHERE date = :date AND time = :time")
    suspend fun getWorkId(date: String, time: String): String?

    @Query("SELECT * FROM AlertData")
    fun getAllAlerts():Flow<List<AlertData>>

    @Query("DELETE FROM AlertData WHERE date = :date AND time = :time")
    suspend fun deleteAlert(date: String, time: String):Int

    @Query("DELETE FROM AlertData WHERE isTriggered = 1 ")
    suspend fun deleteOldAlerts():Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeWeather(weather: HomeWeather): Long

    @Query("SELECT * FROM home_weather WHERE id = 1 LIMIT 1")
     fun getHomeWeather(): Flow<HomeWeather?>

}