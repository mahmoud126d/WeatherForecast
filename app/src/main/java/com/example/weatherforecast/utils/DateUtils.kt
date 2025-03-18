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
}

