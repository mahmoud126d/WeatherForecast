package com.example.weatherforecast.home.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.model.DayWeather
import com.example.weatherforecast.model.toCurrentWeather
import com.example.weatherforecast.model.toFiveDaysWeather
import com.example.weatherforecast.model.toHourlyWeather
import com.example.weatherforecast.repository.CurrentWeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import com.example.weatherforecast.utils.Constants
import com.example.weatherforecast.utils.DateUtils
import com.example.weatherforecast.utils.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel(
    private var repo: CurrentWeatherRepository,
    private var locationRepo: LocationRepository,
    private var settingsRepository: SettingsRepository
) : ViewModel() {


    private val _currentWeather = MutableStateFlow<Response>(Response.Loading)
    val currentWeather = _currentWeather.asStateFlow()

    private val _message = MutableLiveData("")
    val message: LiveData<String> = _message

    private val _hourlyWeather = MutableStateFlow<Response>(Response.Loading)
    val hourlyWeather= _hourlyWeather.asStateFlow()

    private val _dailyWeather = MutableStateFlow<Response>(Response.Loading)
    val dailyWeather = _dailyWeather.asStateFlow()

    val location: StateFlow<Location?> = locationRepo.locationFlow

    private val appId = Constants.API_KEY
    private val lat = Constants.LAT
    private val lon = Constants.LON

    fun getCurrentWeather() {
        Log.d(TAG, "getCurrentWeather: ")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getCurrentWeather(lat, lon, appId)

                response
                    .catch { ex ->
                        _currentWeather.value = Response.Failure(ex)
                    }.collect {
                        _currentWeather.value = Response.Success(it.toCurrentWeather())
                    }
            } catch (ex: Exception) {
                _currentWeather.value = Response.Failure(ex)
            }
        }
    }

    fun getDateTime() = DateUtils.getFormattedDateTime()


    fun getHourlyWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getFiveDaysWeather(lat, lon, appId)
                response
                    .catch { ex ->
                        _hourlyWeather.value = Response.Failure(ex)
                    }.collect {
                        val weatherData = it.toHourlyWeather()
                        weatherData.listOfHourlyWeather = weatherData.listOfHourlyWeather.take(8).map { list ->
                            list.time = DateUtils.extractTime(list.time)
                            list
                        }
                        _hourlyWeather.value = Response.Success(weatherData)
                    }
            } catch (ex: Exception) {
                _hourlyWeather.value = Response.Failure(ex)
            }
        }
    }


    fun getDailyWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getFiveDaysWeather(lat, lon, appId)
                response
                    .catch { ex ->
                        _dailyWeather.value = Response.Failure(ex)
                    }.collect {
                        val weatherData = it.toFiveDaysWeather()

                        val groupedByDay = weatherData.listOfDayWeather.groupBy { item ->
                            DateUtils.extractDay(item.time)
                        }
                        val dailyAverages = groupedByDay.map { (day, readings) ->
                            DayWeather(
                                temp = readings.map { it.temp }.average(),
                                icon = readings.groupingBy { it.icon }.eachCount().maxByOrNull { it.value }?.key ?: "",
                                time = day
                            )
                        }

                        val averagedData = weatherData.copy(listOfDayWeather = dailyAverages)
                        _dailyWeather.value = Response.Success(averagedData)
                    }
            } catch (ex: Exception) {
                _dailyWeather.value = Response.Failure(ex)
            }
        }
    }

    fun startLocationUpdates() {
        locationRepo.startLocationUpdates()
    }

    fun stopLocationUpdates() {
        locationRepo.stopLocationUpdates()
    }

    fun formatNumber(number: Double) = settingsRepository.formatNumber(number)

}


