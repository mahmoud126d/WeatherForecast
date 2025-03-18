package com.example.weatherforecast.home.view

import android.health.connect.datatypes.units.Temperature
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.R
import com.example.weatherforecast.home.viewmodel.HomeViewModel
import com.example.weatherforecast.home.viewmodel.HomeViewModelFactory
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl

private const val TAG = "HomeScreen"

@Composable
fun HomeScreen() {
    val factory = HomeViewModelFactory(
        CurrentWeatherRepositoryImpl.getInstance(
            CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService)
        )
    )
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
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
    homeViewModel.getCurrentWeather()
    val currentWeatherState = homeViewModel.currentWeather.observeAsState()
    val messageState = homeViewModel.message.observeAsState()

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
            item {
                WeatherInfoCard(
                    currentWeatherState.value,
                    contentDescription = "",
                )
            }
            item {
                WeatherStateGrid(
                    windSpeed = currentWeatherState.value?.speed,
                    clouds = currentWeatherState.value?.cloud,
                    pressure = currentWeatherState.value?.pressure,
                    humidity = currentWeatherState.value?.humidity
                )
            }
            item {
                WeatherPeriodBox(
                    "Hourly forecast",
                    painterResource(R.drawable.clock)
                )
            }
            item {
                WeatherPeriodBox(
                    "Day forecast",
                    painterResource(R.drawable.clock)
                )
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

//@Preview(showSystemUi = true)
@Composable
fun WeatherInfoCardPreview() {
    WeatherInfoCard(
        CurrentWeather(
            temperature = 22.0,
            humidity = 11,
            description = "decription",
            pressure = 12,
            city = "Suez",
            speed = 434.0,
            cloud = 123
        ), ""
    )
}

@Composable
fun WeatherInfoCard(
    currentWeather: CurrentWeather?,
    contentDescription: String
) {
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
                currentWeather?.city?.let {
                    Text(
                        text = it,
                        fontSize = 22.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "20\u00B0",
                            fontSize = 80.sp,
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = "Feels like ${currentWeather?.temperature}\u00B0",
                            fontSize = 20.sp,
                            modifier = Modifier.offset(y = (-24).dp, x = (-28).dp)
                        )
                    }

                    // Weather Icon and Description
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Image(
                            contentDescription = "",
                            painter = painterResource(id = R.drawable.cloudandsun),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "skyState",
                            fontSize = 22.sp
                        )
                    }
                }

                // Time Row
                Text(
                    text = "time",
                    modifier = Modifier.align(Alignment.Start) // Align to the start
                )
            }
        }
    }
}


//@Preview(showSystemUi = true)
@Composable
fun WeatherStateGrid(
    windSpeed: Double?,
    clouds: Int?,
    pressure: Int?,
    humidity: Int?
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
                "Wind Speed",
                "$windSpeed",
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            WeatherStateCard(
                painterResource(R.drawable.rainy),
                "description",
                "Clouds",
                "$clouds",
                modifier = Modifier.weight(1f)
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
                "Pressure",
                "$pressure",
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            WeatherStateCard(
                painterResource(R.drawable.air),
                "description",
                "Humidity",
                "$humidity",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WeatherStateCard(
    icon: Painter,
    contentDescription: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier
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
                        text = value,
                    )
                }


            }
        }
    }
}

//@Preview(showSystemUi = true)
@Composable
fun WeatherStateCardPreview() {
    WeatherStateCard(
        painterResource(R.drawable.air),
        "description",
        "Wind Speed",
        "12Km/h",
    )
}

//@Preview(showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun WeatherPeriodBox(
    title:String,
    icon:Painter,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.teal_700))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding( 8.dp)
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
                Text("Hourly forecast")
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(30) {
                    HourlyWeatherColumn(
                        "10AM",
                        painterResource(R.drawable.cloudandsun),
                        "10"
                    )
                }
            }
        }
    }
}


@Composable
fun HourlyWeatherColumn(
    time:String,
    icon:Painter,
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