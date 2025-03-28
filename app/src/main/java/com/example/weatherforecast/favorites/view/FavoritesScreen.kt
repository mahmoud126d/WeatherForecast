package com.example.weatherforecast.favorites.view

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModel
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModelFactory
import com.example.weatherforecast.home.view.RefreshableScreen
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl
import com.example.weatherforecast.utils.Constants
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.SwipeToDismiss
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberDismissState
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissDirection
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.LocationManager
import com.example.weatherforecast.repository.LocationRepository
import kotlinx.coroutines.launch

lateinit var favoritesViewModel: FavoritesViewModel

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val context = LocalContext.current

    val factory = FavoritesViewModelFactory(
        CurrentWeatherRepositoryImpl.getInstance(
            CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService),
            WeatherLocalDataSourceImp(
                WeatherDataBase.getInstance(context).getWeatherDao()
            ),

        ),
       LocationRepository(LocationManager(context)),
    )
    favoritesViewModel = viewModel(factory = factory)
    LaunchedEffect(Unit) {
        favoritesViewModel.getAllFavorites()
        favoritesViewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Constants.FAVORITE_MAP_SCREEN)
                },
                containerColor = Color.Blue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FavoriteColumn(
                favoritesViewModel = favoritesViewModel,
                navController = navController
            )
            LaunchedEffect(Unit) {
                favoritesViewModel.getAllFavorites()
                favoritesViewModel.toastEvent.collect { message ->
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short,
                            actionLabel = "Undo"
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {

                            }

                            SnackbarResult.Dismissed -> {

                            }
                        }
                    }

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FavoriteColumn(
    modifier: Modifier = Modifier,
    favoritesViewModel: FavoritesViewModel,
    navController: NavHostController
) {
    val favoriteWeatherState = favoritesViewModel.productFavoriteList.collectAsState()

    LazyColumn(modifier = modifier) {
        items(favoriteWeatherState.value.size) { index ->
            val item = favoriteWeatherState.value[index]
            val dismissState = rememberDismissState(
                confirmStateChange = { dismissValue ->
                    if (dismissValue == DismissValue.DismissedToStart) {
                        favoritesViewModel.deleteFromFavorite(item)
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismiss(
                state = dismissState,
                directions = setOf(DismissDirection.EndToStart),
                background = {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color.White)
//                            .padding(16.dp),
//                        contentAlignment = Alignment.CenterEnd
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Delete,
//                            contentDescription = "Delete",
//                            tint = Color.White,
//                            modifier = Modifier.size(40.dp)
//                        )
//                    }
                },
                dismissContent = {
                    WeatherItem(
                        country = "Egypt",
                        fullAddress = item.city,
                        navigate = {
                            favoritesViewModel.getWeather(item.city)
                            navController.navigate(Constants.FAVORITE_WEATHER_SCREEN)
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun WeatherItem(
    country: String,
    fullAddress: String,
    navigate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navigate() }
            .clip(RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = country,
                fontSize = 40.sp
            )
            Text(
                text = fullAddress,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = ""
            )
        }
    }
}

@Preview(showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun ItemPreview(modifier: Modifier = Modifier) {
    WeatherItem(
        "Egypt",
        "Suez"
    ) {}
}

@Composable
fun FavoriteWeatherScreen(modifier: Modifier = Modifier) {


    val currentWeatherState = favoritesViewModel.currentWeather.collectAsState()
    val hourlyWeatherState = favoritesViewModel.hourlyWeather.collectAsState()
    val dailyWeatherState = favoritesViewModel.dailyWeather.collectAsState()

    RefreshableScreen(
        currentWeatherState = currentWeatherState,
        hourlyWeatherState = hourlyWeatherState,
        dailyWeatherState = dailyWeatherState,
        onRefresh = {

        },
    )
    Log.d(
        "FavoritesViewModTAGel",
        "getCurrentWeather: ${favoritesViewModel.currentWeather.collectAsState().value}"
    )
}