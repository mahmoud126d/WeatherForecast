package com.example.weatherforecast.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.repository.CurrentWeatherRepository

class HomeViewModelFactory(
    private var repo: CurrentWeatherRepository
) :ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repo) as T
    }
}