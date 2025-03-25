package com.example.weatherforecast.utils

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {


    fun getFormattedDateTime(): String {
        val sdf = SimpleDateFormat("MMMM dd, HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
    fun getFormattedDateTime(pattern:String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
    }
    fun extractTime(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("h a", Locale.getDefault())
        return try {
            val date = inputFormat.parse(dateTime)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            dateTime.substring(11, 16)
        }
    }
    fun extractDay(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("E", Locale.getDefault())
        return try {
            val date = inputFormat.parse(dateTime)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }
    }
}

