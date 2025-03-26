package com.example.weatherforecast.favorites.view

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModel
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModel.Companion
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModelFactory
import com.example.weatherforecast.home.view.RefreshableScreen
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl

//@Composable
//fun FavoriteWeatherScreen(modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//
//    val factory = FavoritesViewModelFactory(
//        CurrentWeatherRepositoryImpl.getInstance(
//            CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService),
//            WeatherLocalDataSourceImp(
//                WeatherDataBase.getInstance(context).getWeatherDao()
//            )
//        )
//    )
//    val favoritesViewModel: FavoritesViewModel = viewModel(factory = factory)
//
//    val currentWeatherState = favoritesViewModel.currentWeather.collectAsState()
//    val hourlyWeatherState = favoritesViewModel.hourlyWeather.collectAsState()
//    val dailyWeatherState = favoritesViewModel.dailyWeather.collectAsState()
//
//    RefreshableScreen(
//        currentWeatherState = currentWeatherState,
//        hourlyWeatherState = hourlyWeatherState,
//        dailyWeatherState = dailyWeatherState,
//        onRefresh = {
//
//        },
//    )
//    Log.d("FavoritesViewModTAGel", "getCurrentWeather: ${favoritesViewModel.currentWeather.collectAsState().value}")
//}