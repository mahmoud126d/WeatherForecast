package com.example.weatherforecast.home.viewmodel

import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.home.viewmodel.HomeViewModel.Companion.DEFAULT_UNIT
import com.example.weatherforecast.utils.AndroidConnectivityObserver
import com.example.weatherforecast.model.DayWeather
import com.example.weatherforecast.model.toCurrentWeather
import com.example.weatherforecast.model.toFiveDaysWeather
import com.example.weatherforecast.model.toHomeWeather
import com.example.weatherforecast.model.toHourlyWeather
import com.example.weatherforecast.repository.WeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import com.example.weatherforecast.utils.Constants
import com.example.weatherforecast.utils.DateUtils
import com.example.weatherforecast.utils.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val weatherRepository: WeatherRepository,
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

    fun getHomeDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            AndroidConnectivityObserver.isConnected.collect {
                if (isOnline) {
                    getHourlyWeather()
                    getDailyWeather()
                    getCurrentWeather()
                } else {
                    getStoredWeather()
                }
            }
        }
    }


    private fun getCurrentWeather(){
        viewModelScope.launch(Dispatchers.IO) {
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
                _currentWeather.value = Response.Failure(ex)
            }
        }
    }
    private fun getHourlyWeather(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (latitude, longitude) = getLocationCoordinates()
                val unit =
                    settingsRepository.temperatureUnitFlow.first() ?: DEFAULT_UNIT

                weatherRepository.getFiveDaysWeather(
                    latitude,
                    longitude,
                    unit,
                    "en",
                    Constants.API_KEY
                )
                    .catch { ex -> _hourlyWeather.value = Response.Failure(ex) }
                    .collect { response ->
                        val todayDate =
                            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())

                        val weatherData = response.toHourlyWeather().apply {
                            listOfHourlyWeather = listOfHourlyWeather
                                .filter { it.time.startsWith(todayDate) }
                                .map { it.apply { time = DateUtils.extractTime(time) } }
                        }
                        _hourlyWeather.value = Response.Success(weatherData)
                    }
            } catch (ex: Exception) {
                _hourlyWeather.value = Response.Failure(ex)
            }
        }
    }
    private fun getDailyWeather(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (latitude, longitude) = getLocationCoordinates()
                val unit =
                    settingsRepository.temperatureUnitFlow.first() ?: DEFAULT_UNIT

                weatherRepository.getFiveDaysWeather(
                    latitude,
                    longitude,
                    unit,
                    "en",
                    Constants.API_KEY
                )
                    .catch { ex -> _dailyWeather.value = Response.Failure(ex) }
                    .collect { response ->
                        val weatherData = response.toFiveDaysWeather()
                        val dailyAverages =
                            calculateDailyAverages(weatherData.listOfDayWeather)
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
    private fun getStoredWeather(){
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
        val groupedByDay = weatherList.groupBy {
            it.time.split(" ")[0]
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())

        return groupedByDay.entries
            .sortedBy { (dateString, _) ->
                dateFormat.parse(dateString)
            }
            .take(5)
            .map { (dateString, readings) ->
                val date = dateFormat.parse(dateString)
                val dayName = dayNameFormat.format(date)

                DayWeather(
                    temp = readings.map { it.temp }.average(),
                    icon = readings.groupingBy { it.icon }.eachCount()
                        .maxByOrNull { it.value }?.key ?: "",
                    time = dayName
                )
            }
    }


    suspend fun getTemperatureUnit(): String {
        return settingsRepository.temperatureUnitFlow.first() ?: DEFAULT_UNIT
    }


    fun startLocationUpdates() = locationRepository.startLocationUpdates()


    private fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return formatter.format(calendar.time)
    }

}