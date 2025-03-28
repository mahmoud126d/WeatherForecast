package com.example.weatherforecast.alarms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.home.viewmodel.HomeViewModel
import com.example.weatherforecast.repository.CurrentWeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository

class AlarmsViewModelFactory(
    private var repo: CurrentWeatherRepository,
    private var locationRepo: LocationRepository
) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlarmsViewModel(repo, locationRepo) as T
    }
}


