package com.example.weatherforecast.home.viewmodel

import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.AndroidConnectivityObserver
import com.example.weatherforecast.ConnectivityRepository
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.model.DayWeather
import com.example.weatherforecast.model.toCurrentWeather
import com.example.weatherforecast.model.toFiveDaysWeather
import com.example.weatherforecast.model.toHomeWeather
import com.example.weatherforecast.model.toHourlyWeather
import com.example.weatherforecast.repository.CurrentWeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import com.example.weatherforecast.utils.Constants
import com.example.weatherforecast.utils.DateUtils
import com.example.weatherforecast.utils.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.log

class HomeViewModel(
    private val weatherRepository: CurrentWeatherRepository,
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_UNIT = "metric"
        private const val MAX_HOURLY_FORECASTS = 8
    }

    // State flows for different weather data
    private val _currentWeather = MutableStateFlow<Response>(Response.Loading)
    val currentWeather: StateFlow<Response> = _currentWeather.asStateFlow()

    private val _hourlyWeather = MutableStateFlow<Response>(Response.Loading)
    val hourlyWeather: StateFlow<Response> = _hourlyWeather.asStateFlow()

    private val _dailyWeather = MutableStateFlow<Response>(Response.Loading)
    val dailyWeather: StateFlow<Response> = _dailyWeather.asStateFlow()


    // Location state
    val location: StateFlow<Location?> = locationRepository.locationFlow

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private var isOnline = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            AndroidConnectivityObserver.isConnected.collect {
                isOnline = AndroidConnectivityObserver.isConnected.first()
            }
        }
    }

    fun isOnline() = isOnline

    @RequiresApi(Build.VERSION_CODES.O)
    fun getHomeDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            AndroidConnectivityObserver.isConnected.collect {
                if (isOnline) {
                    viewModelScope.launch(Dispatchers.IO) {
                        //_toastEvent.emit("You Are online")
                        try {
                            val (latitude, longitude) = getLocationCoordinates()
                            val unit =
                                settingsRepository.temperatureUnitFlow.first() ?: DEFAULT_UNIT
                            val language =
                                settingsRepository.languageFlow.first() ?: DEFAULT_LANGUAGE

                            weatherRepository.getCurrentWeather(
                                latitude,
                                longitude,
                                unit,
                                language,
                                Constants.API_KEY
                            )
                                .catch { ex -> _currentWeather.value = Response.Failure(ex) }
                                .collect { response ->
                                    _currentWeather.value =
                                        Response.Success(response.toCurrentWeather().apply {
                                            lastUpdate = getCurrentDateTime()
                                        })
                                }
                        } catch (ex: Exception) {
                            Log.d(TAG, "getHomeDetails: ${ex.message}")
                            _currentWeather.value = Response.Failure(ex)
                        }
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val (latitude, longitude) = getLocationCoordinates()
                            val unit =
                                settingsRepository.temperatureUnitFlow.first() ?: DEFAULT_UNIT

                            weatherRepository.getFiveDaysWeather(
                                latitude,
                                longitude,
                                unit,
                                "ar",
                                Constants.API_KEY
                            )
                                .catch { ex -> _hourlyWeather.value = Response.Failure(ex) }
                                .collect { response ->
                                    val weatherData = response.toHourlyWeather().apply {
                                        listOfHourlyWeather = listOfHourlyWeather
                                            .take(MAX_HOURLY_FORECASTS)
                                            .map { it.apply { time = DateUtils.extractTime(time) } }
                                    }
                                    _hourlyWeather.value = Response.Success(weatherData)
                                }
                        } catch (ex: Exception) {
                            _hourlyWeather.value = Response.Failure(ex)
                        }
                        viewModelScope.launch(Dispatchers.IO) {
                            try {

                                val (latitude, longitude) = getLocationCoordinates()
                                val unit =
                                    settingsRepository.temperatureUnitFlow.first() ?: DEFAULT_UNIT

                                weatherRepository.getFiveDaysWeather(
                                    latitude,
                                    longitude,
                                    unit,
                                    "ar",
                                    Constants.API_KEY
                                )
                                    .catch { ex -> _dailyWeather.value = Response.Failure(ex) }
                                    .collect { response ->
                                        val weatherData = response.toFiveDaysWeather()
                                        val dailyAverages =
                                            calculateDailyAverages(weatherData.listOfDayWeather)
                                        weatherData.lastUpdate = getCurrentDateTime()
                                        _dailyWeather.value = Response.Success(
                                            weatherData.copy(
                                                listOfDayWeather = dailyAverages,
                                            )
                                        )

                                        weatherRepository.insertHomeWeather(
                                            weatherData.copy(
                                                listOfDayWeather = dailyAverages,
                                            ).toHomeWeather()
                                        )

                                    }
                            } catch (ex: Exception) {
                                _dailyWeather.value = Response.Failure(ex)

                            }
                        }
                    }
                } else {
                    //_toastEvent.emit("connection lost")
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            weatherRepository.getHomeWeather().collect {
                                if (it != null) {
                                    _currentWeather.value = Response.Success(it.toCurrentWeather())
                                    _hourlyWeather.value = Response.Success(it.toCurrentWeather())
                                    _dailyWeather.value = Response.Success(it.toCurrentWeather())
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }


    private suspend fun getLocationCoordinates(): Pair<Double, Double> {
        val locationSelection = settingsRepository.locationSelection.first()
        return if (locationSelection == "gps") {
            val location = locationRepository.locationFlow.filterNotNull().first()
            Pair(location.latitude, location.longitude)
        } else {
            val longitude = settingsRepository.longFlow.first() ?: 0.0
            val latitude = settingsRepository.latFlow.first() ?: 0.0
            Pair(latitude, longitude)
        }
    }

    private fun calculateDailyAverages(weatherList: List<DayWeather>): List<DayWeather> {
        val groupedByDay = weatherList.groupBy { DateUtils.extractDay(it.time) }
        return groupedByDay.map { (day, readings) ->
            DayWeather(
                temp = readings.map { it.temp }.average(),
                icon = readings.groupingBy { it.icon }.eachCount()
                    .maxByOrNull { it.value }?.key
                    ?: "",
                time = day
            )
        }
    }

    suspend fun getTemperatureUnit(): String {
        return settingsRepository.temperatureUnitFlow.first() ?: DEFAULT_UNIT
    }

    fun getDateTime() = DateUtils.getFormattedDateTime()

    fun startLocationUpdates() = locationRepository.startLocationUpdates()

    fun stopLocationUpdates() = locationRepository.stopLocationUpdates()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDateTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        return current.format(formatter)
    }

}