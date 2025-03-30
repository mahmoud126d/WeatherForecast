package com.example.weatherforecast.favorites.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.AndroidConnectivityObserver
import com.example.weatherforecast.home.viewmodel.HomeViewModel
import com.example.weatherforecast.home.viewmodel.HomeViewModel.Companion
import com.example.weatherforecast.model.CurrentWeather
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Stack

class FavoritesViewModel(
    private val weatherRepository: CurrentWeatherRepository,
    private var locationRepo: LocationRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_UNIT = "metric"
        private const val MAX_HOURLY_FORECASTS = 8
    }

    private val _weatherFavoriteList = MutableStateFlow<List<CurrentWeather>>(emptyList())
    val productFavoriteList = _weatherFavoriteList.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private val _internetToastEvent = MutableSharedFlow<String>()
    val internetToastEvent = _internetToastEvent.asSharedFlow()

    // State flows for different weather data
    private val _currentWeather = MutableStateFlow<Response>(Response.Loading)
    val currentWeather: StateFlow<Response> = _currentWeather.asStateFlow()

    private val _hourlyWeather = MutableStateFlow<Response>(Response.Loading)
    val hourlyWeather: StateFlow<Response> = _hourlyWeather.asStateFlow()

    private val _dailyWeather = MutableStateFlow<Response>(Response.Loading)
    val dailyWeather: StateFlow<Response> = _dailyWeather.asStateFlow()


    private val deletedWeatherStack = Stack<CurrentWeather>()


    val cityName: StateFlow<String?> = locationRepo.cityNameFlow

    private var lastDeleted: CurrentWeather? = null

    private var isOnline = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            AndroidConnectivityObserver.isConnected.collect {
                isOnline = AndroidConnectivityObserver.isConnected.first()
            }
        }
    }

    fun isOnline() = isOnline


    private fun calculateDailyAverages(weatherList: List<DayWeather>): List<DayWeather> {
        val groupedByDay = weatherList.groupBy { DateUtils.extractDay(it.time) }
        return groupedByDay.map { (day, readings) ->
            DayWeather(
                temp = readings.map { it.temp }.average(),
                icon = readings.groupingBy { it.icon }.eachCount().maxByOrNull { it.value }?.key
                    ?: "",
                time = day
            )
        }
    }

    fun getAllFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.getAllWeather()?.collect {
                val list: List<CurrentWeather> = it
                _weatherFavoriteList.value = list
            }
        }
    }

    fun deleteFromFavorite(weather: CurrentWeather) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (weatherRepository.deleteWeather(weather) > 0) {
                    //mutableMessage.postValue("deleted")
                    //getFavoriteProducts()
                    deletedWeatherStack.push(weather)
                    _toastEvent.emit("deleted from Favorite")
                } else {
                    //mutableMessage.postValue("error")
                    _toastEvent.emit("failed to delete from Favorite")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun undoDelete() {
        viewModelScope.launch(Dispatchers.IO) {
            if (deletedWeatherStack.isNotEmpty()) {
                val lastDeleted = deletedWeatherStack.pop()
                weatherRepository.insertWeather(lastDeleted)
            }
        }
    }

    private fun saveWeather(weather: CurrentWeather) {
        Log.d(TAG, "saveWeather: ")
        viewModelScope.launch(Dispatchers.IO) {
            val cityName = locationRepo.cityNameFlow.first() ?: "No City Name"
            weather.address = cityName
            weather.country = getCountryName(weather.country ?: "")

            if (weatherRepository.insertWeather(weather) < 0) {
                _toastEvent.emit("failed to add to Favorite")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeather(longitude: Double, latitude: Double) {
        if (isOnline) {
            var unit: String
            var language: String
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    unit =
                        settingsRepository.temperatureUnitFlow.first() ?: DEFAULT_UNIT
                    language =
                        settingsRepository.languageFlow.first() ?: DEFAULT_LANGUAGE
                    weatherRepository.getCurrentWeather(
                        latitude,
                        longitude,
                        unit,
                        language,
                        Constants.API_KEY
                    )
                        .catch {
                            //ex -> _currentWeather.value = Response.Failure(ex)
                                ex ->
                            _toastEvent.emit("error from API: ${ex.message}")
                        }
                        .collect { response ->
//                        response.toCurrentWeather()
                            response.toCurrentWeather().apply {
                                lastUpdate = getCurrentDateTime()
                            }
                            //Log.d(TAG, "getDailyWeather:${response.toCurrentWeather()} ")
                            // weatherRepository.insertWeather(response.toCurrentWeather())
                        }
                } catch (ex: Exception) {
                    //_currentWeather.value = Response.Failure(ex)
                    _toastEvent.emit("error from API: ${ex.message}")
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    weatherRepository.getFiveDaysWeather(
                        latitude,
                        longitude,
                        "metric",
                        "en",
                        Constants.API_KEY
                    )
                        .catch {
                            // ex -> _hourlyWeather.value = Response.Failure(ex)
                        }
                        .collect { response ->
                            val weatherData = response.toHourlyWeather().apply {
                                listOfHourlyWeather = listOfHourlyWeather
                                    .take(MAX_HOURLY_FORECASTS)
                                    .map { it.apply { time = DateUtils.extractTime(time) } }
                            }
                            Log.d(TAG, "getDailyWeather:${weatherData} ")
                            //    _hourlyWeather.value = Response.Success(weatherData)
                        }
                } catch (ex: Exception) {
                    // _hourlyWeather.value = Response.Failure(ex)
                }
            }

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    weatherRepository.getFiveDaysWeather(
                        latitude,
                        longitude,
                        "metric",
                        "en",
                        Constants.API_KEY
                    )
                        .catch {
                            //ex -> _dailyWeather.value = Response.Failure(ex)
                        }
                        .collect { response ->
                            val weatherData = response.toFiveDaysWeather()
                            val dailyAverages = calculateDailyAverages(weatherData.listOfDayWeather)
                            _dailyWeather.value = Response.Success(
                                weatherData.copy(
                                    listOfDayWeather = dailyAverages,
                                )
                            )
                            Log.d(
                                TAG,
                                "getDailyWeather:${weatherData.copy(listOfDayWeather = dailyAverages)} "
                            )
                            //weatherRepository.insertWeather(weatherData.copy(listOfDayWeather = dailyAverages))
                            saveWeather(weatherData.copy(listOfDayWeather = dailyAverages))

                        }
                } catch (ex: Exception) {
                    //_dailyWeather.value = Response.Failure(ex)
                }
            }
        } else {

            Log.d(TAG, "fav offline")
            viewModelScope.launch(Dispatchers.IO) {
                weatherRepository.getWeather(longitude, latitude).collect {
                    Log.d(TAG, "getWeather: long : ${longitude} lat : $latitude")
                    val currentWeather: CurrentWeather? = it
                    if (currentWeather != null) {
                        Log.d(TAG, "getWeather: not null")
                        _currentWeather.value = Response.Success(currentWeather)
                        _hourlyWeather.value = Response.Success(currentWeather)
                        _dailyWeather.value = Response.Success(currentWeather)
                    }
                }
            }
        }
    }
    suspend fun getTemperatureUnit(): String {
        return settingsRepository.temperatureUnitFlow.first() ?: DEFAULT_UNIT
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDateTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        return current.format(formatter)
    }

    private fun getCountryName(countryCode: String): String {
        return Locale("", countryCode).displayCountry
    }

}