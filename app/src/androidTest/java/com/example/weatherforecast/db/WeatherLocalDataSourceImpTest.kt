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

        val result = weatherLocalDataSourceImp.insertHomeWeather(homeWeather)


        assertTrue(result > 0)
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

        weatherLocalDataSourceImp.insertHomeWeather(homeWeather)

        val result = weatherLocalDataSourceImp.getHomeWeather().first()

        assertNotNull(result)
        assertThat(result?.id, `is`(20))
        assertThat(result?.city, `is`("Suez"))
        assertThat(result?.description, `is`("Clear Sky"))
    }

    @After
    fun tearDown() {
        db.close()
    }
}
