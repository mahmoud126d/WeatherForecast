package com.example.weatherforecast

import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class NotificationViewModel(private val workManager: WorkManager) : ViewModel() {

    fun scheduleNotification(long: Double, lat: Double, date: String, time: String) {
        val delay = timeToMillis(date, time)
        val data = workDataOf(
            "KEY_LONG" to long,
            "KEY_LAT" to lat
        )

        val myWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        workManager.enqueue(myWorkRequest)
    }

    private fun timeToMillis(targetDate: String, targetTime: String): Long {
        val fullFormat = "MM/dd/yyyy hh:mm a"
        val sdf = SimpleDateFormat(fullFormat, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()

        return try {
            val targetDateTime = sdf.parse("$targetDate $targetTime")
            val currentTime = System.currentTimeMillis()
            targetDateTime?.time?.minus(currentTime) ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    fun cancelNotifications() {
        workManager.cancelAllWork()
    }
}