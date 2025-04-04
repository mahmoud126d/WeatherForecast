package com.example.weatherforecast.home.view

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.db.DataStoreManager
import com.example.weatherforecast.utils.LanguageHelper
import com.example.weatherforecast.R
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.home.viewmodel.HomeViewModel
import com.example.weatherforecast.home.viewmodel.HomeViewModelFactory
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import com.example.weatherforecast.repository.WeatherRepositoryImpl

private const val TAG = "HomeScreen"

private const val MY_LOCATION_PERMISSION_ID = 5005
 var tempUnitSymbol: String = ""
 var speedUnitSymbol: String = ""

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val factory = HomeViewModelFactory(
        WeatherRepositoryImpl.getInstance(
            CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService),
            WeatherLocalDataSourceImp(
                WeatherDataBase.getInstance(context).getWeatherDao()
            )
        ),
        LocationRepository(com.example.weatherforecast.utils.LocationManager(context)),
        SettingsRepository(
            DataStoreManager(context.applicationContext),
            LanguageHelper
        )

    )
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    LaunchedEffect(Unit) {
        homeViewModel.getHomeDetails()
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
        when (unit) {
            "metric" -> {
                tempUnitSymbol = context.getString(R.string.c)
                speedUnitSymbol = context.getString(R.string.m_s)
            }

            "imperial" -> {
                tempUnitSymbol = context.getString(R.string.f)
                speedUnitSymbol = context.getString(R.string.mph)
            }

            else -> {
                tempUnitSymbol = context.getString(R.string.k)
                speedUnitSymbol = ""
            }
        }
        homeViewModel.getHomeDetails()
//        homeViewModel.toastEvent.collect { message ->
//            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//
//        }
    }
    
    RefreshableScreen(
        currentWeatherState = homeViewModel.currentWeather.collectAsState(),
        hourlyWeatherState = homeViewModel.hourlyWeather.collectAsState(),
        dailyWeatherState = homeViewModel.dailyWeather.collectAsState(),
        onRefresh = {
            homeViewModel.getHomeDetails()
            if (!homeViewModel.isOnline()) {
                Toast.makeText(context,
                    context.getString(R.string.check_you_re_internet_connection), Toast.LENGTH_SHORT).show()
            }
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