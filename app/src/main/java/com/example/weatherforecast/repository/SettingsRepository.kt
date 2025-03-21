package com.example.weatherforecast.repository


import com.example.weatherforecast.DataStoreManager
import com.example.weatherforecast.LanguageChangeHelper


class SettingsRepository(private val dataStoreManager: DataStoreManager,private val languageChangeHelper: LanguageChangeHelper) {
    val languageFlow = dataStoreManager.languageFlow
    val temperatureUnitFlow = dataStoreManager.tempUnitFlow

    suspend fun saveLanguage(lang: String) {
        dataStoreManager.saveLanguage(lang)
    }

    suspend fun saveTemperatureUnit(unit: String) {
        dataStoreManager.saveTemperatureUnit(unit)
    }
     fun getDefaultLanguage() = languageChangeHelper.getDefaultLanguage()
    fun changeLanguage(langCode:String) = languageChangeHelper.changeLanguage(langCode)
}