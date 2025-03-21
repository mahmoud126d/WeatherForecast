package com.example.weatherforecast

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Warning
import com.example.weatherforecast.utils.Constants


sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen(Constants.HOME_SCREEN, "Home", Icons.Default.Home)
    data object Alarm : Screen(Constants.ALARM_SCREEN, "Alarms", Icons.Default.Warning)
    data object Favorite : Screen(Constants.FAVORITES_SCREEN, "Favorites", Icons.Default.Favorite)
    data object Settings : Screen(Constants.SETTINGS_SCREEN, "Settings", Icons.Default.Settings)
    data object Map : Screen(Constants.MAP_SCREEN, "map", Icons.Default.LocationOn)
}
