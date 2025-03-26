package com.example.weatherforecast.home.view

import android.util.Log
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherforecast.LanguageChangeHelper
import com.example.weatherforecast.R
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModel
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModelFactory
import com.example.weatherforecast.home.viewmodel.HomeViewModel
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl
import com.example.weatherforecast.utils.Response
import kotlinx.coroutines.delay

private const val TAG = "RefreshableScreen"




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableScreen(
    currentWeatherState: State<Response>,
    hourlyWeatherState: State<Response>,
    dailyWeatherState: State<Response>,
    onRefresh: suspend () -> Unit,
    //homeViewModel: HomeViewModel
) {




    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(pullToRefreshState.isRefreshing) {
            onRefresh()
            delay(500)
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
                    item {
                        LoadingAnimation()
                    }
                }

                is Response.Success -> {
                    val currentWeatherResponse = currentWeather.data

                    item {
                        WeatherInfoCard(
                            currentWeatherResponse,
                            contentDescription = "",
                            formattedDateTime = "homeViewModel.getDateTime()",
                        )
                    }
                    item {
                        WeatherStateGrid(
                            windSpeed = currentWeatherResponse.speed,
                            clouds = currentWeatherResponse.cloud,
                            pressure = currentWeatherResponse.pressure,
                            humidity = currentWeatherResponse.humidity,
                        )
                    }


                }

                is Response.Failure -> {
                    Log.d(TAG, "failed")
                }
            }
            when (val hourlyWeather2 = hourlyWeatherState.value) {
                is Response.Failure -> {
                    Log.d(TAG, "Failure")
                }

                Response.Loading -> {
                    Log.d(TAG, "Loading")
                    item {
                        LoadingAnimation()
                    }
                }

                is Response.Success -> {
                    val currentWeather = hourlyWeather2.data
                    item {
                        WeatherPeriodBox(
                            stringResource(R.string.hourly_forecast),
                            painterResource(R.drawable.clock),
                            currentWeather,
                        )

                    }
                }
            }
            when (val dailyWeather2 = dailyWeatherState.value) {
                is Response.Failure -> {
                    Log.d(TAG, "Failure")
                }

                Response.Loading -> {
                    Log.d(TAG, "Loading")
                }

                is Response.Success -> {
                    val currentWeather = dailyWeather2.data
                    item {
                        WeatherPeriodBox(
                            stringResource(R.string.day_forecast),
                            painterResource(R.drawable.clock),
                            currentWeather,
                        )
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
        )
    }

}


@Composable
fun WeatherInfoCard(
    currentWeather: CurrentWeather,
    formattedDateTime: String,
    contentDescription: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
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
                verticalArrangement = Arrangement.SpaceBetween,

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
                            text = "${LanguageChangeHelper.formatNumber(currentWeather.temperature.toInt())}${tempUnitSymbol}",
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
                            fontSize = 24.sp,
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
            )
            Spacer(Modifier.width(16.dp))
            WeatherStateCard(
                painterResource(R.drawable.rainy),
                "description",
                stringResource(R.string.clouds),
                clouds.toDouble(),
                modifier = Modifier.weight(1f),
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
            )
            Spacer(Modifier.width(16.dp))
            WeatherStateCard(
                painterResource(R.drawable.air),
                "description",
                stringResource(R.string.humidity),
                humidity.toDouble(),
                modifier = Modifier.weight(1f),
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
) {
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
                        text = LanguageChangeHelper.formatNumber(value.toInt()),

                        )
                }


            }
        }
    }
}


//@Preview(showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun WeatherPeriodBox(
    title: String,
    icon: Painter,
    currentWeather: CurrentWeather,
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

                if (title == title2) {
                    items(currentWeather.listOfHourlyWeather.size) { index ->
                        HourlyWeatherColumn(
                            currentWeather.listOfHourlyWeather[index].time,
                            painterResource(R.drawable.cloudandsun),
                            "${LanguageChangeHelper.formatNumber(currentWeather.listOfHourlyWeather[index].temp.toInt())}${tempUnitSymbol}"
                        )
                    }
                } else {
                    items(currentWeather.listOfDayWeather.size) { index ->
                        HourlyWeatherColumn(

                            currentWeather.listOfDayWeather[index].time,
                            painterResource(R.drawable.cloudandsun),
                            "${LanguageChangeHelper.formatNumber(currentWeather.listOfDayWeather[index].temp.toInt())}${tempUnitSymbol}"
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

@Composable
fun LoadingAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.Url("https://lottie.host/47e59a10-000b-4f22-a3cf-412e18d01770/hff86ngThj.lottie"))
    LottieAnimation(composition=composition, iterations = LottieConstants.IterateForever)
}

