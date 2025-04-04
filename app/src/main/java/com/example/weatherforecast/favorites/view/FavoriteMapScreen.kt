package com.example.weatherforecast.favorites.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.weatherforecast.db.DataStoreManager
import com.example.weatherforecast.utils.LanguageHelper
import com.example.weatherforecast.utils.LocationManager
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModel
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModelFactory
import com.example.weatherforecast.map.view.MapScreen
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.WeatherRepositoryImpl
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import com.example.weatherforecast.utils.Constants

private const val TAG = "FavoriteMapScreen"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FavoriteMapScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {

    val context = LocalContext.current

    val factory = FavoritesViewModelFactory(
        WeatherRepositoryImpl.getInstance(
            CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService),
            WeatherLocalDataSourceImp(
                WeatherDataBase.getInstance(context).getWeatherDao()
            )
        ),
        LocationRepository(LocationManager(context)),
        SettingsRepository(
            DataStoreManager(context.applicationContext),
            LanguageHelper
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
            favoritesViewModel.getWeather(longitude, latitude)
            navController.navigate(Constants.FAVORITES_SCREEN)
        }
    )
}