package com.example.weatherforecast.repository


import android.content.Context
import com.example.weatherforecast.db.DataStoreManager
import com.example.weatherforecast.utils.LanguageHelper


class SettingsRepository(
    private val dataStoreManager: DataStoreManager,
    private val languageChangeHelper: LanguageHelper
) {
    val languageFlow = dataStoreManager.languageFlow
    val temperatureUnitFlow = dataStoreManager.tempUnitFlow
    val locationSelection = dataStoreManager.locationMethodFlow
    val longFlow = dataStoreManager.longFlow
    val latFlow = dataStoreManager.latFlow
    suspend fun saveLanguage(lang: String) {
        dataStoreManager.saveLanguage(lang)
    }

    suspend fun saveTemperatureUnit(unit: String) {
        dataStoreManager.saveTemperatureUnit(unit)
    }
    suspend fun saveLocationSelection(selection:String){
        dataStoreManager.saveLocationSelection(selection)
    }
    suspend fun saveLongitude(longitude:Double){
        dataStoreManager.saveLongitude(longitude)
    }
    suspend fun saveLatitude(latitude:Double){
        dataStoreManager.saveLatitude(latitude)
    }

    fun getDefaultLanguage(): String = languageChangeHelper.getDefaultLanguage()
    fun changeLanguage(context: Context,langCode: String) = languageChangeHelper.changeLanguage(context,langCode)

}