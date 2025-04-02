package com.example.weatherforecast.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.repository.WeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository

class HomeViewModelFactory(
    private var weatherRepo: WeatherRepository,
    private var locationRepo : LocationRepository,
    private var settingsRepository: SettingsRepository,

    ) :ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(weatherRepo,locationRepo,settingsRepository) as T
    }
}