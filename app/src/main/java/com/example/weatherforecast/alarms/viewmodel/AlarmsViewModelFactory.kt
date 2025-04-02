package com.example.weatherforecast.alarms.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.repository.WeatherRepository
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository

class AlarmsViewModelFactory(
    private var repo: WeatherRepository,
    private var locationRepo: LocationRepository,
    private val application: Application,
    private val weatherRepository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlarmsViewModel(repo, locationRepo,application,weatherRepository,settingsRepository) as T
    }
}


