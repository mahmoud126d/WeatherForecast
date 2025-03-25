package com.example.weatherforecast.favorites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.repository.CurrentWeatherRepository

class FavoritesViewModelFactory(
    private var repo: CurrentWeatherRepository,

) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavoritesViewModel(repo) as T
    }
}