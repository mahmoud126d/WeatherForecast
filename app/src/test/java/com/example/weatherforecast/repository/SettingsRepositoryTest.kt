package com.example.weatherforecast.repository


import android.content.Context
import com.example.weatherforecast.db.DataStoreManager
import com.example.weatherforecast.utils.LanguageHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
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


}
