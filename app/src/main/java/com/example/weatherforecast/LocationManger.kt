package com.example.weatherforecast

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.util.Locale

class LocationManager(context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val _locationFlow = MutableStateFlow<Location?>(null)
    val locationFlow: StateFlow<Location?> = _locationFlow.asStateFlow()

    private val _addressFlow = MutableStateFlow<Address?>(null)
    val addressFlow: StateFlow<Address?> = _addressFlow.asStateFlow()

    private val geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var  locationCallback:LocationCallback

    private val _cityNameFlow = MutableStateFlow<String?>(null)
    val cityNameFlow: StateFlow<String?> = _cityNameFlow.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(60).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        }.build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                _locationFlow.value = locationResult.lastLocation
            }
        }
        fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
    }




    fun getCityNameFromLocation(latitude: Double, longitude: Double) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    val address = addresses.getOrNull(0)
                    val city = address?.subAdminArea
                    _cityNameFlow.value = city ?: "Unknown City"
                    Log.d("LocationManager", "City Name: ${address}")
                }
            } else {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.getOrNull(0)
                val city = address?.subAdminArea
                _cityNameFlow.value = city ?: "Unknown City"
                Log.d("LocationManager", "City Name: $city")
            }
        } catch (e: IOException) {
            Log.e("LocationManager", "Geocoder failed: ${e.message}")
        }
    }


}
