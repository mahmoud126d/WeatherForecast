package com.example.weatherforecast.favorites.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.home.viewmodel.HomeViewModel
import com.example.weatherforecast.home.viewmodel.HomeViewModel.Companion
import com.example.weatherforecast.utils.AndroidConnectivityObserver
import com.example.weatherforecast.model.WeatherData
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Stack

class FavoritesViewModel(
    private val weatherRepository: WeatherRepository,
    private var locationRepo: LocationRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_UNIT = "metric"
        private const val MAX_HOURLY_FORECASTS = 8
    }

    private val _weatherFavoriteList = MutableStateFlow<List<WeatherData>>(emptyList())
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


    private val deletedWeatherStack = Stack<WeatherData>()


    val cityName: StateFlow<String?> = locationRepo.cityNameFlow


    private var isOnline = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            AndroidConnectivityObserver.isConnected.collect {
                isOnline = AndroidConnectivityObserver.isConnected.first()
            }
        }
    }

    fun isOnline() = isOnline


    fun getAllFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.getAllWeather()?.collect {
                val list: List<WeatherData> = it
                _weatherFavoriteList.value = list
            }
        }
    }

    fun deleteFromFavorite(weather: WeatherData) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (weatherRepository.deleteWeather(weather) > 0) {

                    deletedWeatherStack.push(weather)
                    _toastEvent.emit("deleted from Favorite")
                } else {
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

    fun getWeather(longitude: Double, latitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            AndroidConnectivityObserver.isConnected.collect {
                if (isOnline) {
                    getHourlyWeather(longitude, latitude)
                    getDailyWeather(longitude, latitude)
                    getCurrentWeather(longitude, latitude)

                } else {
                    getStoredWeather()
                }
            }
        }
    }


    private fun getHourlyWeather(longitude: Double, latitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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

    private fun getDailyWeather(longitude: Double, latitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.getFiveDaysWeather(
                    latitude,
                    longitude,
                    "metric",
                    "en",
                    Constants.API_KEY
                )
                    .catch { ex -> _hourlyWeather.value = Response.Failure(ex) }
                    .collect { response ->
                        val todayDate =
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.ENGLISH
                            ).format(Date())

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

    private fun getCurrentWeather(longitude: Double, latitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {


                weatherRepository.getFiveDaysWeather(
                    latitude,
                    longitude,
                    "metric",
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
                        saveWeather(weatherData.copy(listOfDayWeather = dailyAverages))

                    }
            } catch (ex: Exception) {
                _dailyWeather.value = Response.Failure(ex)

            }
        }
    }

    private fun getStoredWeather() {
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

    private fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    private fun getCountryName(countryCode: String): String {
        return Locale("", countryCode).displayCountry
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

    private fun saveWeather(weather: WeatherData) {
        viewModelScope.launch(Dispatchers.IO) {
            val cityName = locationRepo.cityNameFlow.first() ?: "No City Name"
            weather.address = cityName
            weather.country = getCountryName(weather.country ?: "")

            if (weatherRepository.insertWeather(weather) < 0) {
                _toastEvent.emit("failed to add to Favorite")
            }
        }
    }

    suspend fun getTemperatureUnit(): String {
        return settingsRepository.temperatureUnitFlow.first() ?: DEFAULT_UNIT
    }
}