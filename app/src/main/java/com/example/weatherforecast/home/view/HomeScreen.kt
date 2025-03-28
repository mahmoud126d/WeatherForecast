package com.example.weatherforecast.home.view

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.AndroidConnectivityObserver
import com.example.weatherforecast.ConnectivityRepository
import com.example.weatherforecast.ConnectivityViewModel
import com.example.weatherforecast.DataStoreManager
import com.example.weatherforecast.LanguageChangeHelper
import com.example.weatherforecast.R
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.favorites.view.favoritesViewModel
import com.example.weatherforecast.home.viewmodel.HomeViewModel
import com.example.weatherforecast.home.viewmodel.HomeViewModelFactory
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import kotlinx.coroutines.flow.first

private const val TAG = "HomeScreen"

private const val MY_LOCATION_PERMISSION_ID = 5005
lateinit var tempUnitSymbol: String

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val locationManager = com.example.weatherforecast.LocationManager(context)

    val factory = HomeViewModelFactory(
        CurrentWeatherRepositoryImpl.getInstance(
            CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService),
            WeatherLocalDataSourceImp(
                WeatherDataBase.getInstance(context).getWeatherDao()
            )
        ),
        LocationRepository(locationManager),
        SettingsRepository(
            DataStoreManager(context.applicationContext),
            LanguageChangeHelper
        ),
        ConnectivityRepository(AndroidConnectivityObserver(
            context = context.applicationContext
        ))

    )
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    LaunchedEffect(Unit) {
        homeViewModel.getConnectivityState()
        homeViewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        }
    }
    LaunchedEffect(Unit) {
        homeViewModel.getCurrentWeather()
        homeViewModel.getHourlyWeather()
        homeViewModel.getDailyWeather()
    }
    LaunchedEffect(Unit) {
        if (checkPermissions(context)) {
            if (isLocationEnabled(context)) {
                homeViewModel.startLocationUpdates()
            } else {
                enableLocationServices(context)
            }
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION,
                ),
                MY_LOCATION_PERMISSION_ID
            )
        }
        val unit = homeViewModel.getTemperatureUnit()
        tempUnitSymbol = when (unit) {
            "metric" -> context.getString(R.string.c)
            "imperial" -> context.getString(R.string.f)
            else -> context.getString(R.string.k)
        }
    }

    RefreshableScreen(
        currentWeatherState = homeViewModel.currentWeather.collectAsState(),
        hourlyWeatherState = homeViewModel.hourlyWeather.collectAsState(),
        dailyWeatherState = homeViewModel.dailyWeather.collectAsState(),
        onRefresh = {
            homeViewModel.getCurrentWeather()
            homeViewModel.getHourlyWeather()
            homeViewModel.getDailyWeather()
        },
    )
}

private fun checkPermissions(context: Context): Boolean {
    var result = false
    if ((ContextCompat.checkSelfPermission(
            context,
            ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
        ||
        (ContextCompat.checkSelfPermission(
            context,
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    ) {
        result = true
    }
    return result
}

private fun isLocationEnabled(context: Context): Boolean {
    val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    )
}


private fun enableLocationServices(context: Context) {
    Toast.makeText(context, "Turn on location", Toast.LENGTH_SHORT).show()
    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}