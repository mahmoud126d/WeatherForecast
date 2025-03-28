package com.example.weatherforecast.alarms.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.repository.CurrentWeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class AlarmsViewModel(
    private var repo: CurrentWeatherRepository,
    private var locationRepo : LocationRepository
) :ViewModel(){

    private val _alertList = MutableStateFlow<List<AlertData>>(emptyList())
    val alertList = _alertList.asStateFlow()


    init {
        getAlerts()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveAlert(city:String, long:Double, lat:Double, date:String, time:String){
        viewModelScope.launch (Dispatchers.IO){
            repo.insertAlert(
                AlertData(
                    city = city,
                    date = date,
                    time = time,
                    long = long,
                    lat = lat,
                    timestamp = convertToTimestamp(date,time)
                )
            )
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertToTimestamp(date: String, time: String): Long {
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a", Locale.ENGLISH)
        val localDateTime = LocalDateTime.parse("$date $time", formatter)
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond()
    }
    fun getAlerts(){
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllAlerts()?.collect{
                val list :List<AlertData> = it
                _alertList.value = list
            }
        }
    }

    fun deleteFromAlerts(alert: AlertData) {
        viewModelScope.launch (Dispatchers.IO){
            repo.deleteAlert(alert)
        }
    }

    fun scheduleNotification(date:String ,time:String) {

    }
}