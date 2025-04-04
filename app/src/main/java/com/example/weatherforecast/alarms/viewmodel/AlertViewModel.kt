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
import com.example.weatherforecast.repository.WeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Stack
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit


class AlertViewModel(
    private var locationRepo : LocationRepository,
    private val application: Application,
    private val weatherRepository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application){

    private val _alertList = MutableStateFlow<List<AlertData>>(emptyList())
    val alertList = _alertList.asStateFlow()

    private val deletedAlertStack = Stack<AlertData>()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    init {
        getAlerts()
        locationRepo.startLocationUpdates()
    }

    private fun getAlerts(){
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.getAllAlerts()?.collect{
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
        val delay = timeToMillis(
            targetDate = alert.date,
            targetTime = alert.time,
        )
        val data = workDataOf(
            "KEY_LONG" to longitude,
            "KEY_LAT" to latitude,
            "KEY_DATE" to alert.date,
            "KEY_TIME" to alert.time
        )

        val myWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(application).enqueue(myWorkRequest)

        weatherRepository.insertAlert(alert.copy(workId = myWorkRequest.id.toString(), lat = latitude, long = longitude, timestamp = convertDateTimeToTimeStamp(alert.date,alert.time)))
    }
}

    fun cancelNotification(date: String, time: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val workIdString = weatherRepository.getWorkId(date, time)
            workIdString?.let {
                val workId = UUID.fromString(it)
                WorkManager.getInstance(application).cancelWorkById(workId)
                weatherRepository.deleteAlert(date, time)

                val alertToDelete = _alertList.value.find { it.date == date && it.time == time }
                alertToDelete?.let {
                    deletedAlertStack.push(it)
                }

                _toastEvent.emit("deleted from Favorite")
            }
        }
    }

    private suspend fun getLocationCoordinates(): Pair<Double, Double> {
        val locationSelection = settingsRepository.locationSelection.first()
        return if (locationSelection == "gps") {
            val location = locationRepo.locationFlow.filterNotNull().first()
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
    fun undoDelete() {
        viewModelScope.launch(Dispatchers.IO) {
            if (deletedAlertStack.isNotEmpty()) {
                val lastDeleted = deletedAlertStack.pop()
                //weatherRepository.insertAlert(lastDeleted)
                scheduleNotification(lastDeleted)
            }
        }
    }

    private fun convertDateTimeToTimeStamp(date:String,time:String):Long{
        val dateStr = "$date $time"
        val format = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH)
        format.timeZone = TimeZone.getTimeZone("UTC")

        val date: Date = format.parse(dateStr)
        val timestamp: Long = date.time / 1000
        return timestamp
    }
    
}