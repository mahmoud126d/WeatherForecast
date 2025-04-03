package com.example.weatherforecast.model

import androidx.room.Entity


@Entity(primaryKeys = ["date", "time"])
data class AlertData(
    val city:String,
    val date:String,
    val time:String,
    val long:Double,
    val lat:Double,
    val timestamp: Long,
    val workId: String,
    val isTriggered:Boolean =false,
)
