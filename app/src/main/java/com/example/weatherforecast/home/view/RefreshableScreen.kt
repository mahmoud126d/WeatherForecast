package com.example.weatherforecast.home.view

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherforecast.LanguageHelper
import com.example.weatherforecast.R
import com.example.weatherforecast.model.WeatherData
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
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),

            ) {

            when (val dailyWeather = dailyWeatherState.value) {
                is Response.Failure -> {
                    item {
                        Text(stringResource(R.string.error_while_loading_weather_data))
                    }
                }

                is Response.Loading -> {
                    item {
                        LoadingAnimation()
                    }
                }

                is Response.Success -> {
                    val weatherData = dailyWeather.data
                    item {
                        WeatherInfoCard(
                            weatherData,
                            contentDescription = "",
                            formattedDateTime = LanguageHelper.formatDateBasedOnLocale(
                                weatherData.lastUpdate
                            ),
                        )
                    }
                    item {
                        WeatherStateGrid(
                            windSpeed = weatherData.speed,
                            clouds = weatherData.cloud,
                            pressure = weatherData.pressure,
                            humidity = weatherData.humidity,
                        )
                    }

                    item {
                        WeatherPeriodBox(
                            stringResource(R.string.today_forecast),
                            painterResource(R.drawable.hour),
                            weatherData,
                        )
                    }

                    item {
                        WeatherPeriodBox(
                            stringResource(R.string.daily_forecast),
                            painterResource(R.drawable.day),
                            weatherData,
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
    weatherData: WeatherData,
    formattedDateTime: String,
    contentDescription: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .clip(RoundedCornerShape(bottomEnd = 24.dp, bottomStart = 24.dp))
    ) {

        Image(
            modifier = Modifier.fillMaxSize(),
            contentDescription = contentDescription,
            painter = painterResource(id = R.drawable.main_image),
            contentScale = ContentScale.Crop
        )
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Text(
                text = "${LanguageHelper.formatNumber(weatherData.temperature.toInt())}${tempUnitSymbol}",
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,

                ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,

                    ) {
                    Text(
                        text = weatherData.city,
                        fontSize = 22.sp
                    )
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = ""
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {

                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Time Row
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.last_update),
                        )
                        Text(
                            text = formattedDateTime,
                            fontSize = 12.sp,
                        )
                    }
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
                            text = weatherData.description,
                            fontSize = 20.sp,
                        )
                    }
                }
            }
        }
    }
}


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
                painterResource(R.drawable.windy),
                "description",
                stringResource(R.string.wind_speed),
                windSpeed,
                unit = speedUnitSymbol,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(16.dp))
            WeatherStateCard(
                painterResource(R.drawable.cloud),
                "description",
                stringResource(R.string.clouds),
                clouds.toDouble(),
                unit = "%",
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
                painterResource(R.drawable.pressure_gauge),
                "description",
                stringResource(R.string.pressure),
                hPaToPercentage(pressure),
                unit = "%",
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(16.dp))
            WeatherStateCard(
                painterResource(R.drawable.humidity),
                "description",
                stringResource(R.string.humidity),
                humidity.toDouble(),
                unit = "%",
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
    unit: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardColors(
                colorResource(id = R.color.purple_alpha_30),
                contentColor = Color.Black,
                disabledContainerColor = Color.Red,
                disabledContentColor = Color.Red,
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                                .size(24.dp)
                                .clip(RoundedCornerShape(25.dp)),
                            contentDescription = contentDescription,
                            painter = icon
                        )
                    }
                }

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
                        text = "${LanguageHelper.formatNumber(value.toInt())}$unit",

                        )
                }
            }
        }
    }
}

@Composable
fun WeatherPeriodBox(
    title: String,
    icon: Painter,
    weatherData: WeatherData,
) {
    val hourlyForecastString = stringResource(R.string.today_forecast)
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(id = R.color.purple_alpha_30))
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
                            .size(25.dp),
                        contentDescription = "",
                        painter = icon
                    )
                }
                Text(
                    title,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyRow(
                reverseLayout = layoutDirection == LayoutDirection.Rtl,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (title == hourlyForecastString) {
                    val hourlyWeatherList = if (layoutDirection == LayoutDirection.Rtl) {
                        weatherData.listOfHourlyWeather.reversed()
                    } else {
                        weatherData.listOfHourlyWeather
                    }
                    items(hourlyWeatherList.size) { index ->
                        HourlyWeatherColumn(
                            hourlyWeatherList[index].time,
                            painterResource(R.drawable.cloudandsun),
                            "${LanguageHelper.formatNumber(hourlyWeatherList[index].temp.toInt())}${tempUnitSymbol}"
                        )
                    }
                } else {
                    val dailyWeatherList = if (layoutDirection == LayoutDirection.Rtl) {
                        weatherData.listOfDayWeather.reversed()
                    } else {
                        weatherData.listOfDayWeather
                    }
                    items(dailyWeatherList.size) { index ->
                        HourlyWeatherColumn(
                            dailyWeatherList[index].time,
                            painterResource(R.drawable.cloudandsun),
                            "${LanguageHelper.formatNumber(dailyWeatherList[index].temp.toInt())}${tempUnitSymbol}"
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
    Box(
        modifier = Modifier.clip(
            RoundedCornerShape(
                bottomEnd = 20.dp,
                topEnd = 20.dp,
                topStart = 20.dp
            )
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(color = colorResource(R.color.purple_alpha_70))
                .padding(12.dp),
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
}

@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.loading_animation))
    val animationState by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true
    )
    LottieAnimation(
        composition = composition,
        progress = { animationState },
        modifier = modifier.fillMaxSize().wrapContentSize()
    )

}

private fun hPaToPercentage(hPa: Int): Double {
    val standardPressure = 1013.25
    return (hPa.toDouble() / standardPressure) * 100
}
