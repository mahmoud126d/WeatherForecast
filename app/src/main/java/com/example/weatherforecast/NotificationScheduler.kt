package com.example.weatherforecast

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private const val TAG = "NotificationScheduler"
object NotificationScheduler {

    fun scheduleNotification(long:Double,lat:Double,date:String ,time:String, context: Context){
        val delay = timeToMillis(
            targetDate = date,
            targetTime = time,
        )
        val data = workDataOf(
            "KEY_LONG" to long,
            "KEY_LAT" to lat,
        )
        val myWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueue(myWorkRequest)


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