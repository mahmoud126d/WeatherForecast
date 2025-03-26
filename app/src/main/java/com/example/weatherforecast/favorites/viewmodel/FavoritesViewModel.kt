package com.example.weatherforecast.favorites.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.model.DayWeather
import com.example.weatherforecast.model.toCurrentWeather
import com.example.weatherforecast.model.toFiveDaysWeather
import com.example.weatherforecast.model.toHourlyWeather
import com.example.weatherforecast.repository.CurrentWeatherRepository
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val weatherRepository: CurrentWeatherRepository
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

    // State flows for different weather data
    private val _currentWeather = MutableStateFlow<Response>(Response.Loading)
    val currentWeather: StateFlow<Response> = _currentWeather.asStateFlow()

    private val _hourlyWeather = MutableStateFlow<Response>(Response.Loading)
    val hourlyWeather: StateFlow<Response> = _hourlyWeather.asStateFlow()

    private val _dailyWeather = MutableStateFlow<Response>(Response.Loading)
    val dailyWeather: StateFlow<Response> = _dailyWeather.asStateFlow()




    fun getCurrentWeather( longitude:Double,latitude:Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.getCurrentWeather(
                    latitude,
                    longitude,
                    "metric",
                    "en",
                    Constants.API_KEY
                )
                    .catch {
                        //ex -> _currentWeather.value = Response.Failure(ex)
                        ex->
                        _toastEvent.emit("error from API: ${ex.message}")
                    }
                    .collect { response ->
                        response.toCurrentWeather()
                        //Log.d(TAG, "getDailyWeather:${response.toCurrentWeather()} ")
                       // weatherRepository.insertWeather(response.toCurrentWeather())
                    }
            } catch (ex: Exception) {
                //_currentWeather.value = Response.Failure(ex)
                _toastEvent.emit("error from API: ${ex.message}")
            }
        }
    }

    fun getHourlyWeather(longitude:Double,latitude:Double) {
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
    }

    fun getDailyWeather(longitude:Double,latitude:Double) {
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
                        Log.d(TAG, "getDailyWeather:${weatherData.copy(listOfDayWeather = dailyAverages)} ")
                        //weatherRepository.insertWeather(weatherData.copy(listOfDayWeather = dailyAverages))
                        saveWeather(weatherData.copy(listOfDayWeather = dailyAverages))
                    }
            } catch (ex: Exception) {
                //_dailyWeather.value = Response.Failure(ex)
            }
        }
    }


    private fun calculateDailyAverages(weatherList: List<DayWeather>): List<DayWeather> {
        val groupedByDay = weatherList.groupBy { DateUtils.extractDay(it.time) }
        return groupedByDay.map { (day, readings) ->
            DayWeather(
                temp = readings.map { it.temp }.average(),
                icon = readings.groupingBy { it.icon }.eachCount().maxByOrNull { it.value }?.key ?: "",
                time = day
            )
        }
    }

    fun getAllFavorites(){
        viewModelScope.launch (Dispatchers.IO){
            weatherRepository.getAllWeather()?.collect{
                val list :List<CurrentWeather> = it
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
                    _toastEvent.emit("deleted from Favorite")
                }else{
                    //mutableMessage.postValue("error")
                    _toastEvent.emit("failed to delete from Favorite")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun saveWeather(weather: CurrentWeather) {
        viewModelScope.launch(Dispatchers.IO) {
            if(weatherRepository.insertWeather(weather)>0){
                _toastEvent.emit("added to Favorite")
            }else{
                _toastEvent.emit("failed to add to Favorite")
            }
        }
    }
    fun getWeather(cityName:String){
        viewModelScope.launch (Dispatchers.IO){
            weatherRepository.getWeather(cityName).collect{
                val currentWeather :CurrentWeather? = it
                if(currentWeather != null){
                    _currentWeather.value = Response.Success(currentWeather)
                    _hourlyWeather.value = Response.Success(currentWeather)
                    _dailyWeather.value = Response.Success(currentWeather)
                }
            }
        }
    }
}