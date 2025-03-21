package com.example.weatherforecast.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.weatherforecast.repository.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val language: LiveData<String?> = repository.languageFlow.asLiveData()
    val tempUnit: LiveData<String?> = repository.temperatureUnitFlow.asLiveData()

    fun saveLanguage(lang: String) {
        viewModelScope.launch {
            repository.saveLanguage(lang)
        }
    }
    fun saveTemperatureUnit(unit: String) {
        viewModelScope.launch {
            repository.saveTemperatureUnit(unit)
        }
    }
}
