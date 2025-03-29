package com.example.weatherforecast.favorites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.ConnectivityRepository
import com.example.weatherforecast.repository.CurrentWeatherRepository
import com.example.weatherforecast.repository.LocationRepository

class FavoritesViewModelFactory(
    private var repo: CurrentWeatherRepository,
    private var locationRepo: LocationRepository,
    private val connectivityRepository: ConnectivityRepository

    ) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavoritesViewModel(repo,locationRepo,connectivityRepository) as T
    }
}