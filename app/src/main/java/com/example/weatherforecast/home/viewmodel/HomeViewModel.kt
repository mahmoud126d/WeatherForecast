package com.example.weatherforecast.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.model.toCurrentWeather
import com.example.weatherforecast.repository.CurrentWeatherRepository
import com.example.weatherforecast.utils.Constants
import com.example.weatherforecast.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class HomeViewModel(
    private var repo: CurrentWeatherRepository
) : ViewModel() {

    private val _currentWeather = MutableLiveData<CurrentWeather>()
    val currentWeather: LiveData<CurrentWeather> = _currentWeather

    private val _message = MutableLiveData("")
    val message: LiveData<String> = _message

    private val appId = Constants.API_KEY
    private val lat = Constants.LAT
    private val lon = Constants.LON

    fun getCurrentWeather() {
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

    fun getDateTime()=DateUtils.getFormattedDateTime()



}
