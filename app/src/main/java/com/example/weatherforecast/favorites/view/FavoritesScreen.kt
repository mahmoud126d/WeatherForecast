package com.example.weatherforecast.favorites.view

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.weatherforecast.AndroidConnectivityObserver
import com.example.weatherforecast.ConnectivityRepository
import com.example.weatherforecast.LocationManager
import com.example.weatherforecast.R
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModel
import com.example.weatherforecast.favorites.viewmodel.FavoritesViewModelFactory
import com.example.weatherforecast.home.view.RefreshableScreen
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.utils.Constants
import kotlinx.coroutines.launch

lateinit var favoritesViewModel: FavoritesViewModel
private const val TAG = "FavoritesScreen"
@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    Log.d(TAG, "FavoritesScreen: ")
    val context = LocalContext.current

    val factory = FavoritesViewModelFactory(
        CurrentWeatherRepositoryImpl.getInstance(
            CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService),
            WeatherLocalDataSourceImp(
                WeatherDataBase.getInstance(context).getWeatherDao()
            ),

        ),
       LocationRepository(LocationManager(context)),
        ConnectivityRepository(
            AndroidConnectivityObserver(
                context = context.applicationContext
            )
        )
    )
    favoritesViewModel = viewModel(factory = factory)
    LaunchedEffect(Unit) {
        favoritesViewModel.getConnectivityState()
        favoritesViewModel.getAllFavorites()

    }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        containerColor = colorResource(id = R.color.background_color),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Constants.FAVORITE_MAP_SCREEN)
                },
                containerColor = colorResource(R.color.purple_500),
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "Add",tint = Color.White)
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
                                favoritesViewModel.undoDelete()
                            }

                            SnackbarResult.Dismissed -> {

                            }
                        }
                    }

                }
            }
            LaunchedEffect(Unit) {
                favoritesViewModel.getAllFavorites()
                favoritesViewModel.internetToastEvent.collect { message ->
                    coroutineScope.launch {
                         snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short,
                        )
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

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = favoriteWeatherState.value,
            key = { _, item -> item.lon }
        ) { index, item ->
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Red)
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                dismissContent = {
                    WeatherItem(
                        country = "Egypt",
                        fullAddress = item.city,
                        navigate = {
                            favoritesViewModel.getWeather(item.lon,item.lat)
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
            .height(80.dp)
            .clickable { navigate() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
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
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "",
                tint = colorResource(R.color.purple_500)
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