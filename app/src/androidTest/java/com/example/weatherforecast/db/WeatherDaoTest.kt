package com.example.weatherforecast.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.model.HomeWeather
import com.example.weatherforecast.model.WeatherData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class WeatherDaoTest {

    private lateinit var weatherDao: WeatherDao
    private lateinit var database: WeatherDataBase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDataBase::class.java
        ).build()

        weatherDao = database.getWeatherDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertWeather_shouldInsertWeatherData() = runTest {
        val weather = WeatherData(lat = 10.0, lon = 20.0, temperature = 22.0, description = "Clear")
         weatherDao.insertWeather(weather)

        val result = weatherDao.getWeatherLatLon(20.0, 10.0).first()
        assertNotNull(result)
        assertEquals(weather.lat, result?.lat)
        assertEquals(weather.lon, result?.lon)
    }

    @Test
    fun deleteWeather_shouldDeleteWeatherData() = runTest {
        val weather = WeatherData(lat = 10.0, lon = 20.0, temperature = 22.0, description = "Clear")
        weatherDao.insertWeather(weather)

        val deletedRows = weatherDao.deleteWeather(weather)
        assertEquals(1, deletedRows)

        val retrievedWeather = weatherDao.getWeatherLatLon(20.0, 10.0).first()
        assertNull(retrievedWeather)
    }



}
