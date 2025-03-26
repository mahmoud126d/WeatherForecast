package com.example.weatherforecast.favorites.view

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModel
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModelFactory
import com.example.weatherforecast.map.view.MapScreen
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl
import com.example.weatherforecast.utils.Constants

private const val TAG = "FavoriteMapScreen"

@Composable
fun FavoriteMapScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {

    val context = LocalContext.current

    val factory = FavoritesViewModelFactory(
        CurrentWeatherRepositoryImpl.getInstance(
            CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService),
            WeatherLocalDataSourceImp(
                WeatherDataBase.getInstance(context).getWeatherDao()
            )
        )
    )
    val favoritesViewModel: FavoritesViewModel = viewModel(factory = factory)
    LaunchedEffect(Unit) {
        favoritesViewModel.getAllFavorites()
    }

    MapScreen(
        navController = navController,
        buttonText = "Set Home Location",
        onButtonClick = { latitude, longitude ->
            // Now you can use latitude and longitude directly
            Log.d("ProfileScreen", "Selected Location: Lat $latitude, Lng $longitude")
            favoritesViewModel.getCurrentWeather(longitude,latitude)
            favoritesViewModel.getHourlyWeather(longitude,latitude)
            favoritesViewModel.getDailyWeather(longitude,latitude)
            // Example: Save to ViewModel or local storage
            //viewModel.saveHomeLocation(latitude, longitude)

            // Navigate to home screen
            navController.navigate(Constants.FAVORITES_SCREEN)
        }
    )
}