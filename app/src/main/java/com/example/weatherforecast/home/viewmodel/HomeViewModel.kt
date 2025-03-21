package com.example.weatherforecast.home.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.model.toCurrentWeather
import com.example.weatherforecast.model.toFiveDaysWeather
import com.example.weatherforecast.repository.CurrentWeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.utils.Constants
import com.example.weatherforecast.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "HomeViewModel"

class HomeViewModel(
    private var repo: CurrentWeatherRepository,
    private var locationRepo :LocationRepository
) : ViewModel() {

    private val _currentWeather = MutableLiveData<CurrentWeather>()
    val currentWeather: LiveData<CurrentWeather> = _currentWeather

    private val _message = MutableLiveData("")
    val message: LiveData<String> = _message

    private val _hourlyWeatherMap = MutableLiveData<Map<String, String>>()
    val hourlyWeatherMap: LiveData<Map<String, String>> = _hourlyWeatherMap

    private val _dailyWeatherMap = MutableLiveData<Map<String, String>>()
    val dailyWeatherMap: LiveData<Map<String, String>> = _dailyWeatherMap

    val location: StateFlow<Location?> = locationRepo.locationFlow

    private val appId = Constants.API_KEY
    private val lat = Constants.LAT
    private val lon = Constants.LON

    fun getCurrentWeather() {
        Log.d(TAG, "getCurrentWeather: ")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getCurrentWeather(lat, lon, appId)

                if (response != null) {
                    val currentWeather = response.toCurrentWeather()
                    _currentWeather.postValue(currentWeather)
                    _message.postValue("Weather data fetched successfully")
                } else {
                    _message.postValue("Failed to fetch weather data: Response is null")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _message.postValue("Failed to fetch weather data: ${e.message}")
            }
        }
    }

    fun getDateTime() = DateUtils.getFormattedDateTime()


    fun getHourlyWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getFiveDaysWeather(lat, lon, appId)
                if (response != null) {
                    val currentWeather = response.toFiveDaysWeather()
                    val dayWeather = currentWeather
                        .take(8)
                        .associate {
                            DateUtils.extractTime(it.date) to it.temperature.toString()
                        }

                    withContext(Dispatchers.Main) {
                        _hourlyWeatherMap.value = dayWeather
                    }
                } else {
                    Log.d(TAG, "Error fetching weather data")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun getDailyWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getFiveDaysWeather(lat, lon, appId)
                if (response != null) {
                    val currentWeather = response.toFiveDaysWeather()

                    val dailyWeather = currentWeather
                        .groupBy { DateUtils.extractDay(it.date) }
                        .mapValues { (_, entries) ->
                            val avgTemp = entries.map { it.temperature }.average() // Calculate average temperature
                            "${avgTemp.toInt()}°C" // Convert to integer and format as "23°C"
                        }

                    withContext(Dispatchers.Main) {
                        _dailyWeatherMap.value = dailyWeather // Update LiveData
                    }
                } else {
                    Log.d(TAG, "Error fetching weather data")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startLocationUpdates() {
        locationRepo.startLocationUpdates()
    }

    fun stopLocationUpdates() {
        locationRepo.stopLocationUpdates()
    }
}


