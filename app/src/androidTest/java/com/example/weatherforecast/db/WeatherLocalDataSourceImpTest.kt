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
    fun insertAndRetrieveHomeWeather_shouldReturnSameWeather() = runTest {
        // Given
        val homeWeather = HomeWeather(
            id = 1,
            temperature = 25.5,
            humidity = 60,
            description = "Sunny",
            pressure = 1013,
            city = "Suez",
            speed = 5.5,
            cloud = 0,
            date = "4/4/2025",
            address = "Main Street",
            lastUpdate = "4/4/2025"
        )

        // When
        val insertResult = weatherLocalDataSourceImp.insertHomeWeather(homeWeather)

        // Then
        val result = weatherLocalDataSourceImp.getHomeWeather().first()

        // Debug
        println("Inserted ID: $insertResult")
        println("Retrieved Weather: $result")

        assertThat(insertResult, `is`(1L))
        assertNotNull(result)
        assertThat(result?.city, `is`("Suez"))
        assertThat(result?.temperature, `is`(25.5))
        assertThat(result?.description, `is`("Sunny"))
        assertThat(result?.pressure, `is`(1013))
        assertThat(result?.speed, `is`(5.5))
        assertThat(result?.cloud, `is`(0))
        assertThat(result?.date, `is`("4/4/2025"))
        assertThat(result?.address, `is`("Main Street"))
        assertThat(result?.lastUpdate, `is`("4/4/2025"))
    }

    @After
    fun tearDown() {
        db.close()
    }
}