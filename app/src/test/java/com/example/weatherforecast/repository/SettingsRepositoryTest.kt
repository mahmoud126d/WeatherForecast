package com.example.weatherforecast.repository


import android.content.Context
import com.example.weatherforecast.DataStoreManager
import com.example.weatherforecast.LanguageHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit

@ExperimentalCoroutinesApi
class SettingsRepositoryTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var dataStoreManager: DataStoreManager

    @Mock
    lateinit var languageHelper: LanguageHelper

    @Mock
    lateinit var context: Context

    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        settingsRepository = SettingsRepository(dataStoreManager, languageHelper)
    }

    @Test
    fun `test saveLanguage should call saveLanguage on DataStoreManager`() = runTest {
        // Given
        val lang = "en"

        // When
        settingsRepository.saveLanguage(lang)

        // Then
        verify(dataStoreManager).saveLanguage(lang)
    }

    @Test
    fun `test saveTemperatureUnit should call saveTemperatureUnit on DataStoreManager`() = runTest {
        // Given
        val unit = "metric"

        // When
        settingsRepository.saveTemperatureUnit(unit)

        // Then
        verify(dataStoreManager).saveTemperatureUnit(unit)
    }

    @Test
    fun `test saveLocationSelection should call saveLocationSelection on DataStoreManager`() = runTest {
        // Given
        val selection = "gps"

        // When
        settingsRepository.saveLocationSelection(selection)

        // Then
        verify(dataStoreManager).saveLocationSelection(selection)
    }

    @Test
    fun `test saveLongitude should call saveLongitude on DataStoreManager`() = runTest {
        // Given
        val longitude = 31.0

        // When
        settingsRepository.saveLongitude(longitude)

        // Then
        verify(dataStoreManager).saveLongitude(longitude)
    }

    @Test
    fun `test saveLatitude should call saveLatitude on DataStoreManager`() = runTest {
        // Given
        val latitude = 30.0

        // When
        settingsRepository.saveLatitude(latitude)

        // Then
        verify(dataStoreManager).saveLatitude(latitude)
    }

    @Test
    fun `test getDefaultLanguage should return language from LanguageHelper`() {
        // Given
        val expectedLanguage = "en"
        `when`(languageHelper.getDefaultLanguage()).thenReturn(expectedLanguage)

        // When
        val result = settingsRepository.getDefaultLanguage()

        // Then
        assert(result == expectedLanguage)
        verify(languageHelper).getDefaultLanguage()
    }

    @Test
    fun `test changeLanguage should call changeLanguage on LanguageHelper`() = runTest {
        // Given
        val langCode = "en"

        // When
        settingsRepository.changeLanguage(context, langCode)

        // Then
        verify(languageHelper).changeLanguage(context, langCode)
    }
}
