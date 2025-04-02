package com.example.weatherforecast.favorites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.repository.WeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository

class FavoritesViewModelFactory(
    private var repo: WeatherRepository,
    private var locationRepo: LocationRepository,
    private val settingsRepository: SettingsRepository,


    ) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavoritesViewModel(repo,locationRepo,settingsRepository) as T
    }
}