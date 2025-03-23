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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.DataStoreManager
import com.example.weatherforecast.LanguageChangeHelper
import com.example.weatherforecast.R
import com.example.weatherforecast.home.viewmodel.HomeViewModel
import com.example.weatherforecast.home.viewmodel.HomeViewModelFactory
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import com.example.weatherforecast.utils.Response

private const val TAG = "HomeScreen"

private const val MY_LOCATION_PERMISSION_ID = 5005

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val locationManager = com.example.weatherforecast.LocationManager(context)
    val settingRepository = SettingsRepository(
        DataStoreManager(context.applicationContext),
        LanguageChangeHelper(context)
    )
    val factory = HomeViewModelFactory(
        CurrentWeatherRepositoryImpl.getInstance(
            CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService)
        ),
        LocationRepository(locationManager),
        settingRepository

    )
    val homeViewModel: HomeViewModel = viewModel(factory = factory)

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
    }

    RefreshableScreen(
        items = listOf("hello", "world"),
        onRefresh = { },
        homeViewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableScreen(
    items: List<String>,
    onRefresh: suspend () -> Unit,
    homeViewModel: HomeViewModel
) {
    LaunchedEffect(Unit) {
        homeViewModel.getCurrentWeather()
        homeViewModel.getHourlyWeather()
        homeViewModel.getDailyWeather()
        //homeViewModel.stopLocationUpdates()
    }

    val location by homeViewModel.location.collectAsStateWithLifecycle()
    val currentWeatherState = homeViewModel.currentWeather.collectAsState()
    val messageState = homeViewModel.message.observeAsState()
    val hourlyWeather = homeViewModel.hourlyWeather.collectAsState()
    val dailyWeather = homeViewModel.dailyWeather.collectAsState()

    location?.let {
        //Text(text = "Latitude: ${it.latitude}, Longitude: ${it.longitude}")
        Log.d("TAG", "Latitude: ${it.latitude}, Longitude: ${it.longitude}")
    }

    val temperature = currentWeatherState.value
    Log.d(TAG, temperature.toString())

    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
            pullToRefreshState.endRefresh()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),

            ) {
            when (val currentWeather = currentWeatherState.value) {
                is Response.Loading -> {
                    Log.d(TAG, "Loading")
                }

                is Response.Success -> {
                    val currentWeatherResponse = currentWeather.data

                        item {
                            WeatherInfoCard(
                                currentWeatherResponse,
                                contentDescription = "",
                                formattedDateTime = homeViewModel.getDateTime(),
                                homeViewModel = homeViewModel
                            )
                        }
                        item {
                            WeatherStateGrid(
                                windSpeed = currentWeatherResponse.speed,
                                clouds = currentWeatherResponse.cloud,
                                pressure = currentWeatherResponse.pressure,
                                humidity = currentWeatherResponse.humidity,
                                homeViewModel
                            )
                        }


                }

                is Response.Failure -> {
                    Log.d(TAG, "failed")
                }
            }
            when(val hourlyWeather2 = hourlyWeather.value){
                is Response.Failure -> {

                }
                Response.Loading -> {

                }
                is Response.Success -> {
                    val currentWeather = hourlyWeather2.data
                    item {
                        WeatherPeriodBox(
                            stringResource(R.string.hourly_forecast),
                            painterResource(R.drawable.clock),
                            currentWeather,
                            homeViewModel
                        )

                    }
                }
            }
            when(val dailyWeather2 = dailyWeather.value){
                is Response.Failure -> {

                }
                Response.Loading -> {

                }
                is Response.Success -> {
                    val currentWeather = dailyWeather2.data
                    item {
                        WeatherPeriodBox(
                            stringResource(R.string.day_forecast),
                            painterResource(R.drawable.clock),
                            currentWeather,
                            homeViewModel
                        )

                    }
                }
            }
        }
    }

    PullToRefreshContainer(
        state = pullToRefreshState,
        //modifier = Modifier.align(Alignment.TopCenter)
    )
}


//@Preview(showSystemUi = true, device = Devices.PIXEL_4)
//@Composable
//fun WeatherInfoCardPreview() {
//    WeatherInfoCard(
//        CurrentWeather(
//            temperature = 22.0,
//            humidity = 11,
//            description = "decription",
//            pressure = 12,
//            city = "Suez",
//            speed = 434.0,
//            cloud = 123,
//            icon = ""
//        ), "January 18, 16:14",
//        contentDescription = ""
//    )
//}

@Composable
fun WeatherInfoCard(
    currentWeather: CurrentWeather,
    formattedDateTime: String,
    contentDescription: String,
    homeViewModel: HomeViewModel
) {
    // Get the current configuration
    val configuration = LocalConfiguration.current
    // The screen width in dp
    val screenWidthDp = configuration.screenWidthDp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(bottomEnd = 24.dp, bottomStart = 24.dp))
    ) {

        // Background Image
        Image(
            modifier = Modifier.fillMaxSize(),
            contentDescription = contentDescription,
            painter = painterResource(id = R.drawable.main_image),
            contentScale = ContentScale.Crop
        )

        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    Modifier.padding(all = 16.dp)
                ) {
                    Text(
                        text = currentWeather.city,
                        fontSize = 22.sp
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = homeViewModel.formatNumber(currentWeather.temperature)  ,
                            fontSize = 100.sp,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Time Row
                    Text(
                        text = formattedDateTime,
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Image(
                            contentDescription = "",
                            painter = painterResource(id = R.drawable.cloudandsun),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = currentWeather.description,
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }
    }
}


//@Preview(showSystemUi = true)
@Composable
fun WeatherStateGrid(
    windSpeed: Double,
    clouds: Int,
    pressure: Int,
    humidity: Int,
    homeViewModel: HomeViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            WeatherStateCard(
                painterResource(R.drawable.air),
                "description",
                stringResource(R.string.wind_speed),
                windSpeed,
                modifier = Modifier.weight(1f),
                homeViewModel
            )
            Spacer(Modifier.width(16.dp))
            WeatherStateCard(
                painterResource(R.drawable.rainy),
                "description",
                stringResource(R.string.clouds),
                clouds.toDouble(),
                modifier = Modifier.weight(1f),
                homeViewModel
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            WeatherStateCard(
                painterResource(R.drawable.waves),
                "description",
                stringResource(R.string.pressure),
                pressure.toDouble(),
                modifier = Modifier.weight(1f),
                homeViewModel
            )
            Spacer(Modifier.width(16.dp))
            WeatherStateCard(
                painterResource(R.drawable.air),
                "description",
                stringResource(R.string.humidity),
                humidity.toDouble(),
                modifier = Modifier.weight(1f),
                homeViewModel
            )
        }
    }
}

@Composable
fun WeatherStateCard(
    icon: Painter,
    contentDescription: String,
    title: String,
    value: Double,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel
) {
    // Use BoxWithConstraints to get available width
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(25.dp)),
                            contentDescription = contentDescription,
                            painter = icon
                        )
                    }
                }

                // Title and value column
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = homeViewModel.formatNumber(value),

                    )
                }


            }
        }
    }
}

//@Preview(showSystemUi = true)
//@Composable
//fun WeatherStateCardPreview() {
//    WeatherStateCard(
//        painterResource(R.drawable.air),
//        "description",
//        "Wind Speed",
//        "12Km/h",
//    )
//}

//@Preview(showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun WeatherPeriodBox(
    title: String,
    icon: Painter,
    currentWeather: CurrentWeather,
    homeViewModel: HomeViewModel
) {
    val title2 = stringResource(R.string.hourly_forecast)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.teal_700))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(25.dp)),
                        contentDescription = "",
                        painter = icon
                    )
                }
                Text(title)
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                if(title== title2){
                    items(currentWeather.listOfHourlyWeather.size) { index ->
                        HourlyWeatherColumn(
                            currentWeather.listOfHourlyWeather[index].time,
                            painterResource(R.drawable.cloudandsun),
                            homeViewModel.formatNumber(currentWeather.listOfHourlyWeather[index].temp)
                        )
                    }
                }else{
                    items(currentWeather.listOfDayWeather.size) { index ->
                        HourlyWeatherColumn(
                            currentWeather.listOfDayWeather[index].time,
                            painterResource(R.drawable.cloudandsun),
                            homeViewModel.formatNumber(currentWeather.listOfDayWeather[index].temp)
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun HourlyWeatherColumn(
    time: String,
    icon: Painter,
    temperature: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(time)
        Image(
            icon,
            contentDescription = "",
            modifier = Modifier.size(40.dp)
        )
        Text(temperature)
    }
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