package com.example.weatherforecast.settings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.weatherforecast.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val language: StateFlow<String?> = settingsRepository.languageFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val tempUnit: StateFlow<String?> = settingsRepository.temperatureUnitFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val locationSelection: StateFlow<String?> = settingsRepository.locationSelection
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun saveLanguage(lang: String) {
        viewModelScope.launch {
            settingsRepository.saveLanguage(lang)
        }
    }

    fun saveTemperatureUnit(unit: String) {
        viewModelScope.launch {
            settingsRepository.saveTemperatureUnit(unit)
        }
    }

    fun saveLocationSelection(selection: String) {
        viewModelScope.launch {
            settingsRepository.saveLocationSelection(selection)
        }
    }

    fun getDefaultLanguage() {
        settingsRepository.getDefaultLanguage()
    }

    fun changeLanguage(context: Context, langCode: String) {
        settingsRepository.changeLanguage(context, langCode)
    }
}
