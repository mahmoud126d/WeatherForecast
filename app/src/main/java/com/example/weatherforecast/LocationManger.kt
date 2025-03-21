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



    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(10 * 60 * 1000).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        }.build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                _locationFlow.value = locationResult.lastLocation
            }
        }
        fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
       // fusedClient.removeLocationUpdates(locationCallback)
    }



    fun getAddressFromLocation(latitude: Double, longitude: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                if (addresses.isNotEmpty()) {
                    _addressFlow.value = addresses[0]
                } else {
                    //_addressFlow.value = addresses[0]
                }
            }
        } else {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    _addressFlow.value = addresses[0]
                } else {
                    //callback(null)
                }
            } catch (e: IOException) {
               // callback(null)
            }
        }
    }


}
