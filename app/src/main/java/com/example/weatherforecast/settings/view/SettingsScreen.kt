package com.example.weatherforecast.settings.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.DataStoreManager
import com.example.weatherforecast.repository.SettingsRepository
import com.example.weatherforecast.settings.viewmodel.SettingsViewModel
import com.example.weatherforecast.settings.viewmodel.SettingsViewModelFactory

@Preview(showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun SettingsScreen(modifier: Modifier= Modifier) {
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context.applicationContext)
    // Create the repository using DataStoreManager
    val repository = SettingsRepository(dataStoreManager)
    // Create a ViewModelFactory that takes the repository as a dependency
    val factory = SettingsViewModelFactory(repository)

    // Obtain the SettingsViewModel via the viewModel() composable function
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)






    Column(
        modifier=modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        Box (
            Modifier
                .padding(20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Gray)
        ){
            TemperatureUnitSelector(settingsViewModel)
        }
    }
}

@Composable
fun TemperatureUnitSelector(settingsViewModel: SettingsViewModel) {
    val language by settingsViewModel.language.observeAsState()
    val tempUnit by settingsViewModel.tempUnit.observeAsState("celsius")

    Column(modifier = Modifier.padding(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Temperature Icon",
                tint = Color.Red,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Temp Unit",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TemperatureRadioButton(
                "Celsius",
                "°C",
                "celsius",
                tempUnit ?: "celsius"  // Provide default if null
            ) {
                settingsViewModel.saveTemperatureUnit("celsius")
            }
            TemperatureRadioButton(
                "Kelvin",
                "°K",
                "kelvin",
                tempUnit ?: "celsius"  // Provide default if null
            ) {
                settingsViewModel.saveTemperatureUnit("kelvin")
            }
            TemperatureRadioButton(
                "Fahrenheit",
                "°F",
                "fahrenheit",
                tempUnit ?: "celsius"  // Provide default if null
            ) {
                settingsViewModel.saveTemperatureUnit("fahrenheit")
            }
        }
    }
}

@Composable
fun TemperatureRadioButton(label: String, unit: String, value: String, selected: String, onSelect: (String) -> Unit) {


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onSelect(value) }
    ) {
        RadioButton(
            selected = value == selected,
            onClick = { onSelect(value) },
            colors = RadioButtonDefaults.colors(
                selectedColor = Color.Cyan,
                unselectedColor = Color.White
            )
        )
        Text(
            text = "$label $unit",
            fontSize = 16.sp,
            color = Color.White
        )
    }
}