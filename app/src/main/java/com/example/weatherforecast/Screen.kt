package com.example.weatherforecast

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.weatherforecast.utils.Constants


sealed class Screen(val route: String, val title: Int, val icon: ImageVector) {
    data object Home : Screen(Constants.HOME_SCREEN, R.string.Home, Icons.Default.Home)
    data object Alarm : Screen(Constants.ALARM_SCREEN, R.string.alarm, Icons.Default.Notifications)
    data object Favorite : Screen(Constants.FAVORITES_SCREEN, R.string.favorites, Icons.Default.Favorite)
    data object Settings : Screen(Constants.SETTINGS_SCREEN, R.string.settings, Icons.Default.Settings)
}
