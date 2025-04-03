package com.example.weatherforecast.db


import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.model.HomeWeather
import com.example.weatherforecast.model.WeatherData
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class WeatherLocalDataSourceImpTest {

    private lateinit var db: WeatherDataBase
    private lateinit var weatherDao: WeatherDao
    private lateinit var weatherLocalDataSourceImp: WeatherLocalDataSourceImp

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WeatherDataBase::class.java)
            .allowMainThreadQueries()
            .build()

        weatherDao = db.getWeatherDao()
        weatherLocalDataSourceImp = WeatherLocalDataSourceImp(weatherDao)
    }

    @Test
    fun testInsertWeather() = runTest {
        val weatherData = WeatherData(/* mock data */)

        // Insert weather data
        val result = weatherLocalDataSourceImp.insertWeather(weatherData)

        // Verify the insertion
        assertTrue(result > 0)  // Result should be the ID (positive value)
    }

    @Test
    fun testDeleteWeather() = runTest {
        val weatherData = WeatherData(/* mock data */)

        // Insert weather data first
        val insertResult = weatherLocalDataSourceImp.insertWeather(weatherData)

        // Delete weather data
        val deleteResult = weatherLocalDataSourceImp.deleteWeather(weatherData)

        // Verify that the data was deleted
        assertTrue(deleteResult > 0)
    }

    @Test
    fun testGetAllWeather() = runTest {
        val weatherData = WeatherData(/* mock data */)

        // Insert weather data
        weatherLocalDataSourceImp.insertWeather(weatherData)

        // Fetch all weather data
        val weatherList = weatherLocalDataSourceImp.getAllWeather().first()

        // Verify the data is returned
        assertNotNull(weatherList)
        assertTrue(weatherList.isNotEmpty())
    }


    @Test
    fun testDeleteOldAlerts() = runTest {
        val currentTime = System.currentTimeMillis()

        // Delete old alerts (for testing purposes)
        val result = weatherLocalDataSourceImp.deleteOldAlerts(currentTime)

        // Verify the result
        assertTrue(result >= 0)
    }

    @Test
    fun testInsertHomeWeather() = runTest {
        val homeWeather = HomeWeather(
            id = 20,
            temperature = 73.0,
            humidity = 3,
            description = "Clear Sky",
            pressure = 99,
            city = "Suez",
            speed = 30.0,
            cloud = 44,
            date = "4/3/2025",
            address = "",
            lastUpdate = "2/3/2025"
        )

        // Insert home weather
        val result = weatherLocalDataSourceImp.insertHomeWeather(homeWeather)

        // Verify the result (ID should be > 0)
        assertTrue(result > 0)  // Ensure the insertion was successful and returns a valid ID
    }

    @Test
    fun testGetHomeWeather() = runTest {
        val homeWeather = HomeWeather(
            id = 20,
            temperature = 73.0,
            humidity = 3,
            description = "Clear Sky",
            pressure = 99,
            city = "Suez",
            speed = 30.0,
            cloud = 44,
            date = "4/3/2025",
            address = "",
            lastUpdate = "2/3/2025"
        )

        // Insert home weather
        weatherLocalDataSourceImp.insertHomeWeather(homeWeather)

        // Get home weather
        val result = weatherLocalDataSourceImp.getHomeWeather().first()

        // Verify the result
        assertNotNull(result)  // Ensure the result is not null
        assertThat(result?.id, `is`(20))  // Ensure the ID matches the inserted value
        assertThat(result?.city, `is`("Suez"))  // Verify the city is correct
        assertThat(result?.description, `is`("Clear Sky"))  // Check the weather description
    }

    @After
    fun tearDown() {
        db.close()
    }
}
