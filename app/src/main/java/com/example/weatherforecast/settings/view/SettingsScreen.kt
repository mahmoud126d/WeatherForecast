package com.example.weatherforecast.settings.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun SettingsScreen(modifier: Modifier= Modifier) {
    Column(
        modifier=modifier.fillMaxSize().wrapContentSize()
    ) {
        Text("SettingsScreen")
    }
}