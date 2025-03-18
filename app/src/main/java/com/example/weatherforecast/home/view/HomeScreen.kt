package com.example.weatherforecast.home.view

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.home.viewmodel.HomeViewModel
import com.example.weatherforecast.home.viewmodel.HomeViewModelFactory
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
        items = listOf("hello","world"),
        onRefresh = {  },
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
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                Text(text = item)
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
