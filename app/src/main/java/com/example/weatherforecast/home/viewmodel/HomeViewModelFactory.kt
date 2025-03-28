package com.example.weatherforecast.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.ConnectivityRepository
import com.example.weatherforecast.repository.CurrentWeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository

class HomeViewModelFactory(
    private var repo: CurrentWeatherRepository,
    private var locationRepo : LocationRepository,
    private var settingsRepository: SettingsRepository,
    private val connectivityRepository: ConnectivityRepository
) :ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repo,locationRepo,settingsRepository,connectivityRepository) as T
    }
}