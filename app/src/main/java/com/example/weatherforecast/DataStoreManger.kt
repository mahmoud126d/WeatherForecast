package com.example.weatherforecast

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create an extension property for DataStore in Context.
val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    private val language = stringPreferencesKey("language")
    private val temperatureUnit = stringPreferencesKey("temperature_unit")
    private val location = stringPreferencesKey("location_method")
    private val long = stringPreferencesKey("long")
    private val lat = stringPreferencesKey("lat")

    // Expose a Flow that emits the saved user name.
    val languageFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[language]
        }

    val tempUnitFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[temperatureUnit]?: "celsius"
        }
    val locationMethodFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[location]
        }

    val longFlow: Flow<Double?> = context.dataStore.data
        .map { preferences -> preferences[long]?.toDoubleOrNull() }

    val latFlow: Flow<Double?> = context.dataStore.data
        .map { preferences -> preferences[lat]?.toDoubleOrNull() }


    suspend fun saveLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[language] = lang
        }
    }

    suspend fun saveTemperatureUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[temperatureUnit] = unit
        }
    }
    suspend fun saveLocationSelection(selection:String){
        context.dataStore.edit { preferences ->
            preferences[location] = selection
        }
    }
    suspend fun saveLongitude(longitude: Double) {
        context.dataStore.edit { preferences ->
            preferences[long] = longitude.toString()
        }
    }

    suspend fun saveLatitude(latitude: Double) {
        context.dataStore.edit { preferences ->
            preferences[lat] = latitude.toString()
        }
    }
}