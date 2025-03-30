package com.example.weatherforecast.alarms.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.weatherforecast.NotificationWorker
import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.repository.CurrentWeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit


class AlarmsViewModel(
    private var repo: CurrentWeatherRepository,
    private var locationRepo : LocationRepository,
    private val application: Application,
    private val weatherRepository: CurrentWeatherRepository,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application){

    private val _alertList = MutableStateFlow<List<AlertData>>(emptyList())
    val alertList = _alertList.asStateFlow()


    init {
        getAlerts()
        locationRepo.startLocationUpdates()
    }

    private fun getAlerts(){
        viewModelScope.launch(Dispatchers.IO) {
            repo.getAllAlerts()?.collect{
                val list :List<AlertData> = it
                _alertList.value = list
            }
        }
    }

fun scheduleNotification(alert: AlertData) {
    viewModelScope.launch(Dispatchers.IO) {
        val (latitude, longitude) = if (alert.long == 0.0 && alert.lat == 0.0) {
            withContext(Dispatchers.IO) { getLocationCoordinates() }
        } else {
            Pair(alert.lat, alert.long)
        }
        Log.d("TAG", "scheduleNotification: lat $latitude long $longitude")
        val delay = timeToMillis(
            targetDate = alert.date,
            targetTime = alert.time,
        )

        val data = workDataOf(
            "KEY_LONG" to longitude,
            "KEY_LAT" to latitude,
        )

        val myWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(application).enqueue(myWorkRequest)

        weatherRepository.insertAlert(alert.copy(workId = myWorkRequest.id.toString(), lat = latitude, long = longitude))
    }
}

    fun cancelNotification( date: String, time: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val workIdString = weatherRepository.getWorkId(date, time)
            workIdString?.let {
                val workId = UUID.fromString(it)
                WorkManager.getInstance(application).cancelWorkById(workId)
                weatherRepository.deleteAlert(date, time)
            }
        }
    }
    private suspend fun getLocationCoordinates(): Pair<Double, Double> {
        val locationSelection = settingsRepository.locationSelection.first()
        return if (locationSelection == "gps") {
            Log.d("TAG", "before getLocationCoordinates:")
            val location = locationRepo.locationFlow.filterNotNull().first()
            Log.d("TAG", "getLocationCoordinates: ${location.latitude}")
            Pair(location.latitude, location.longitude)
        } else {
            val longitude = settingsRepository.longFlow.first() ?: 0.0
            val latitude = settingsRepository.latFlow.first() ?: 0.0
            Pair(latitude, longitude)
        }
    }
    private fun timeToMillis(targetDate: String, targetTime: String, dateFormat: String = "MM/dd/yyyy", timeFormat: String = "hh:mm a"): Long {
        val fullFormat = "$dateFormat $timeFormat"
        val sdf = SimpleDateFormat(fullFormat, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return try {
            val dateTimeString = "$targetDate $targetTime"
            val targetDateTime = sdf.parse(dateTimeString)
            val currentTime = System.currentTimeMillis()
            targetDateTime?.time?.minus(currentTime) ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
}