package com.example.weatherforecast.map.viewmodel

import android.location.Address
import android.location.Location
import androidx.lifecycle.ViewModel
import com.example.weatherforecast.repository.LocationRepository
import kotlinx.coroutines.flow.StateFlow

class MapViewModel (private var locationRepo : LocationRepository):ViewModel(){
    val address: StateFlow<Address?> = locationRepo.addressFlow

    fun askForAddress(lat:Double,long:Double){
        locationRepo.getAddress(lat,long)
    }

}