package com.example.weatherforecast.repository


import com.example.weatherforecast.DataStoreManager


class SettingsRepository(private val dataStoreManager: DataStoreManager) {
    val languageFlow = dataStoreManager.languageFlow
    val temperatureUnitFlow = dataStoreManager.tempUnitFlow

    suspend fun saveLanguage(lang: String) {
        dataStoreManager.saveLanguage(lang)
    }

    suspend fun saveTemperatureUnit(unit: String) {
        dataStoreManager.saveTemperatureUnit(unit)
    }
}