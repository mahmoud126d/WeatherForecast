package com.example.weatherforecast.repository

import android.location.Address
import android.location.Location
import com.example.weatherforecast.utils.LocationManager
import kotlinx.coroutines.flow.StateFlow

class LocationRepository(private val locationManager: LocationManager) {

    val locationFlow: StateFlow<Location?> = locationManager.locationFlow
    val addressFlow: StateFlow<Address?> = locationManager.addressFlow
    val cityNameFlow : StateFlow<String?> = locationManager.cityNameFlow

    fun startLocationUpdates() {
        locationManager.startLocationUpdates()
    }

    fun stopLocationUpdates() {
        locationManager.stopLocationUpdates()
    }
    fun getAddress(latitude: Double, longitude: Double){
        locationManager.getCityNameFromLocation(latitude,longitude)
    }
}