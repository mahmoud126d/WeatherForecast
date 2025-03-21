package com.example.weatherforecast.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.repository.LocationRepository

class MapViewModelFactory(
    private var locationRepo : LocationRepository
) :ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapViewModel(locationRepo) as T
    }
}
