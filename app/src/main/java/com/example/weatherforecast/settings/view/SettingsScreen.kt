package com.example.weatherforecast.settings.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.weatherforecast.DataStoreManager
import com.example.weatherforecast.LanguageChangeHelper
import com.example.weatherforecast.R
import com.example.weatherforecast.repository.SettingsRepository
import com.example.weatherforecast.settings.viewmodel.SettingsViewModel
import com.example.weatherforecast.settings.viewmodel.SettingsViewModelFactory
import com.example.weatherforecast.utils.Constants
import java.util.Locale

@Composable
fun SettingsScreen(modifier: Modifier = Modifier,navController: NavController) {
    val context = LocalContext.current
    // Create the repository using DataStoreManager
    val repository = SettingsRepository(DataStoreManager(context.applicationContext),LanguageChangeHelper)
    // Create a ViewModelFactory that takes the repository as a dependency
    val factory = SettingsViewModelFactory(repository)

    // Obtain the SettingsViewModel via the viewModel() composable function
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    Log.d("TAG", "SettingsScreen: ${Locale.getDefault().language}")

    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        LanguageBox(
            settingsViewModel = settingsViewModel
        )
        TemperatureBox(
            settingsViewModel = settingsViewModel
        )
        LocationBox(
            settingsViewModel = settingsViewModel,
            navController = navController
        )
    }
}


@Composable
fun LanguageBox(modifier: Modifier = Modifier, settingsViewModel: SettingsViewModel) {
    Box(
        Modifier
            .padding(20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Gray)
    ) {
        LanguageSelector(settingsViewModel)
    }
}

@Composable
fun TemperatureBox(modifier: Modifier = Modifier, settingsViewModel: SettingsViewModel) {
    Box(
        Modifier
            .padding(20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Gray)
    ) {
        TemperatureUnitSelector(settingsViewModel)
    }
}
@Composable
fun LocationBox(modifier: Modifier = Modifier, settingsViewModel: SettingsViewModel,navController:NavController) {
    Box(
        Modifier
            .padding(20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Gray)
    ) {
        LocationSelector(settingsViewModel,navController)
    }
}
@Composable
fun LanguageSelector(settingsViewModel: SettingsViewModel) {
    val language by settingsViewModel.language.observeAsState()
    val context = LocalContext.current
    Column(modifier = Modifier.padding(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Language Icon",
                tint = Color.Red,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.language),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButtonRow(
                stringResource(R.string.ar),
                "ar",
                language ?: "en"
            ) {
                LanguageChangeHelper.changeLanguage(context,"ar")
                settingsViewModel.saveLanguage("ar")
            }
            RadioButtonRow(
                stringResource(R.string.en),
                "en",
                language ?: "en"
            ) {
                LanguageChangeHelper.changeLanguage(context,"en")
                settingsViewModel.saveLanguage("en")
            }
        }
    }
}

@Composable
fun TemperatureUnitSelector(settingsViewModel: SettingsViewModel) {
    val tempUnit by settingsViewModel.tempUnit.observeAsState("celsius")

    Column(modifier = Modifier.padding(4.dp)) {
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
                text = stringResource(R.string.temp_unit),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButtonRow(
                stringResource(R.string.metric),
                "metric",
                tempUnit ?: "metric"
            ) {
                settingsViewModel.saveTemperatureUnit("metric")
            }
            RadioButtonRow(
                stringResource(R.string.standard),
                "standard",
                tempUnit ?: "metric"
            ) {
                settingsViewModel.saveTemperatureUnit("standard")
            }
            RadioButtonRow(
                stringResource(R.string.imperial),
                "imperial",
                tempUnit ?: "metric"
            ) {
                settingsViewModel.saveTemperatureUnit("imperial")
            }
        }
    }
}

@Composable
fun LocationSelector(settingsViewModel: SettingsViewModel,navController:NavController) {
    val locationSelection by settingsViewModel.locationSelection.observeAsState()
    val context = LocalContext.current
    Column(modifier = Modifier.padding(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Language Icon",
                tint = Color.Red,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.location_select),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButtonRow(
                stringResource(R.string.map),
                "map",
                locationSelection ?: "gps"
            ) {
                navController.navigate(Constants.MAP_SCREEN)
                settingsViewModel.saveLocationSelection("map")
            }
            RadioButtonRow(
                stringResource(R.string.gps),
                "gps",
                locationSelection ?: "gps"
            ) {
                settingsViewModel.saveLocationSelection("gps")
            }
        }
    }
}


@Composable
fun RadioButtonRow(
    label: String,
    value: String,
    selected: String,
    onSelect: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable {
            onSelect(value)
        }
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
            text = label,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}