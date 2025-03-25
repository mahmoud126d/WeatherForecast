package com.example.weatherforecast.favorites.view

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import com.example.weatherforecast.model.CurrentWeather
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl
import com.example.weatherforecast.utils.Constants
import kotlinx.coroutines.flow.collect

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
            )
        )
    )
    val favoritesViewModel: FavoritesViewModel = viewModel(factory = factory)
    LaunchedEffect(Unit) {
        favoritesViewModel.getAllFavorites()
        favoritesViewModel.toastEvent.collect{
            message->
            Toast.makeText(context,message,Toast.LENGTH_SHORT).show()

        }
    }
    Scaffold(
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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            // Your main content
            FavoriteColumn(favoritesViewModel = favoritesViewModel)
        }
    }

}

@Composable
fun FavoriteColumn(modifier: Modifier = Modifier,favoritesViewModel: FavoritesViewModel) {
    val favoriteWeatherState = favoritesViewModel.productFavoriteList.collectAsState()

    LazyColumn (){
        items(favoriteWeatherState.value.size){
            index->
            WeatherItem(
                favoriteWeatherState.value[index].city,
                onClick = {
                    favoritesViewModel.deleteFromFavorite(favoriteWeatherState.value[index])
                }
            )
        }
    }
}

@Composable
fun WeatherItem(
    city: String,
    onClick:()->Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(40.dp)
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(city)
            Button({
                onClick()

            }) {
                Text("Delete")
            }
        }
    }
}
