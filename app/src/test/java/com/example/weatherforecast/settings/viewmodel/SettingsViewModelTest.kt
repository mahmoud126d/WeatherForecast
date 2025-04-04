package com.example.weatherforecast.settings.viewmodel


import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherforecast.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Mock
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SettingsViewModel(settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveLanguage_shouldCallRepository() = testScope.runTest {
        val lang = "en"
        viewModel.saveLanguage(lang)
        advanceUntilIdle()
        verify(settingsRepository).saveLanguage(lang)
    }

    @Test
    fun saveTemperatureUnit_shouldCallRepository() = testScope.runTest {
        val unit = "Celsius"
        viewModel.saveTemperatureUnit(unit)
        advanceUntilIdle()
        verify(settingsRepository).saveTemperatureUnit(unit)
    }


}
