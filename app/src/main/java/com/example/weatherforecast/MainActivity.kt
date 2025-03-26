package com.example.weatherforecast

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherforecast.alarms.view.AlarmsScreen
import com.example.weatherforecast.favorites.view.FavoriteMapScreen
import com.example.weatherforecast.favorites.view.FavoriteWeatherScreen
import com.example.weatherforecast.favorites.view.FavoritesScreen
import com.example.weatherforecast.home.view.HomeScreen
import com.example.weatherforecast.home.view.RefreshableScreen
import com.example.weatherforecast.map.view.MapScreen
import com.example.weatherforecast.settings.view.SettingsScreen
import com.example.weatherforecast.ui.theme.WeatherForecastTheme
import com.example.weatherforecast.utils.Constants
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            WeatherForecastTheme {
                val navController = rememberNavController()
                MainScreen(navController)
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    BackHandler {
        if (currentRoute != Constants.HOME_SCREEN) {
            navController.navigate(Constants.HOME_SCREEN) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        } else {
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        topBar = { TopAppBar(navController) },
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val screensWithBottomBar = listOf(
                Constants.HOME_SCREEN,
                Constants.ALARM_SCREEN,
                Constants.FAVORITES_SCREEN,
                Constants.SETTINGS_SCREEN
            )

            if (currentRoute in screensWithBottomBar) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Constants.HOME_SCREEN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Constants.HOME_SCREEN) { HomeScreen() }
            composable(Constants.ALARM_SCREEN) { AlarmsScreen() }
            composable(Constants.FAVORITES_SCREEN) { FavoritesScreen(navController = navController) }
            composable(Constants.SETTINGS_SCREEN) { SettingsScreen(navController = navController) }
            composable(Constants.MAP_SCREEN) { MapScreen(navController = navController) }
            composable(Constants.FAVORITE_MAP_SCREEN) { FavoriteMapScreen(navController = navController) }
            composable(Constants.FAVORITE_WEATHER_SCREEN) { FavoriteWeatherScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(navController: NavController) {
    val canNavigateBack = navController.previousBackStackEntry != null

    androidx.compose.material3.TopAppBar(
        title = { Text("Weather Forecast", color = Color.Black) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        },

    )
}


@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(Screen.Home, Screen.Alarm, Screen.Favorite, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.Transparent
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription =stringResource(screen.title)) },
                label = { Text(stringResource(screen.title)) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Constants.HOME_SCREEN) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
