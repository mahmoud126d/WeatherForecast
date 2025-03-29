package com.example.weatherforecast.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.model.HomeWeather
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: CurrentWeather):Long

    @Delete
    suspend fun deleteWeather(weather: CurrentWeather):Int

    @Query("SELECT * FROM CurrentWeather")
    fun getAllWeather():Flow<List<CurrentWeather>>

    @Query("SELECT * FROM CurrentWeather WHERE lat = :latitude AND lon = :longitude LIMIT 1")
    fun getWeatherLatLon( longitude: Double,latitude: Double): Flow<CurrentWeather?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alertData: AlertData):Long

    @Query("SELECT * FROM AlertData")
    fun getAllAlerts():Flow<List<AlertData>>

    @Delete
    suspend fun deleteAlert(alertData: AlertData):Int

    @Query("DELETE FROM AlertData WHERE timestamp < :currentTime")
    suspend fun deleteOldAlerts(currentTime: Long):Int


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeWeather(weather: HomeWeather): Long

    @Query("SELECT * FROM home_weather WHERE id = 1 LIMIT 1")
     fun getHomeWeather(): Flow<HomeWeather?>

}