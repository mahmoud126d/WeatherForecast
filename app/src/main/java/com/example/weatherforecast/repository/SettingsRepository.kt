package com.example.weatherforecast.repository


import android.content.Context
import com.example.weatherforecast.DataStoreManager
import com.example.weatherforecast.LanguageChangeHelper
import java.util.UUID


class SettingsRepository(
    private val dataStoreManager: DataStoreManager,
    private val languageChangeHelper: LanguageChangeHelper
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
//    suspend fun saveWorkId(workId: UUID){
//        dataStoreManager.saveWorkId(workId)
//    }
//    suspend fun getWorkIds(workId: UUID){
//        dataStoreManager.clearWorkId(workId)
//    }
    fun getDefaultLanguage(): String = languageChangeHelper.getDefaultLanguage()
    fun changeLanguage(context: Context,langCode: String) = languageChangeHelper.changeLanguage(context,langCode)
    fun formatNumber(number: Int) = languageChangeHelper.formatNumber(number)

}