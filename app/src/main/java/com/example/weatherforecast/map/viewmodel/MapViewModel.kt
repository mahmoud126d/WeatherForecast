package com.example.weatherforecast.map.viewmodel

import android.location.Address
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel (private var locationRepo : LocationRepository,private var settingsRepository: SettingsRepository):ViewModel(){
    val address: StateFlow<Address?> = locationRepo.addressFlow
    val cityName: StateFlow<String?> = locationRepo.cityNameFlow

    fun askForAddress(lat:Double,long:Double){
        locationRepo.getAddress(lat,long)
    }
    fun saveLongitude(long: Double){
        viewModelScope.launch {
            settingsRepository.saveLongitude(long)
        }
    }
    fun saveLatitude(lat: Double){
        viewModelScope.launch {
            settingsRepository.saveLatitude(lat)
        }
    }
}