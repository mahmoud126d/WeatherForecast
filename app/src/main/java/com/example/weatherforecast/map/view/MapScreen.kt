package com.example.weatherforecast.map.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.weatherforecast.DataStoreManager
import com.example.weatherforecast.LanguageHelper
import com.example.weatherforecast.LocationManager
import com.example.weatherforecast.map.viewmodel.MapViewModel
import com.example.weatherforecast.map.viewmodel.MapViewModelFactory
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

// MapScreen now takes custom parameters for button text and onClick
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    buttonText: String = "Select Location",
    onButtonClick: (Double, Double) -> Unit = { lat, lng ->
        // Default implementation if no custom handler is provided
        navController.navigateUp()
    }
) {
    val context = LocalContext.current
    val locationManager = LocationManager(context)
    val factory = MapViewModelFactory(
        LocationRepository(locationManager),
        SettingsRepository(
            DataStoreManager(context.applicationContext),
            LanguageHelper
        )
    )
    val mapViewModel: MapViewModel = viewModel(factory = factory)

    // Local state for latitude & longitude
    var latLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }

    Map(
        modifier = modifier,
        navController = navController,
        buttonText = buttonText,
        onButtonClick = {
            onButtonClick(latLng.latitude, latLng.longitude)
            mapViewModel.saveLatitude(latLng.latitude)
            mapViewModel.saveLongitude(latLng.longitude)
        },
        mapViewModel = mapViewModel,
        onLocationSelected = { newLatLng ->
            latLng = newLatLng
        }
    )
}

// Map composable modified to accept custom button text
@Composable
fun Map(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    buttonText: String,
    onButtonClick: () -> Unit,
    mapViewModel: MapViewModel,
    onLocationSelected: (LatLng) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        GoogleMapScreen(mapViewModel, onLocationSelected)
        LocationSelectionCard(
            navController = navController,
            mapViewModel = mapViewModel,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 64.dp)
                .offset(y= (-60).dp)
            .alpha(0.8f),
            buttonText = buttonText,
            onButtonClick = onButtonClick
        )
    }
}

// GoogleMapScreen remains the same as in your original implementation
@Composable
fun GoogleMapScreen(
    mapViewModel: MapViewModel,
    onLocationSelected: (LatLng) -> Unit
) {
    var markerPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, 1f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            markerPosition = latLng
            onLocationSelected(latLng)
            mapViewModel.askForAddress(latLng.latitude, latLng.longitude)
        }
    ) {
        Marker(
            state = MarkerState(position = markerPosition),
            title = "Selected Location"
        )
    }
}

// LocationSelectionCard now accepts custom button text
@Composable
fun LocationSelectionCard(
    modifier: Modifier = Modifier,
    navController: NavController,
    mapViewModel: MapViewModel,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    val address by mapViewModel.address.collectAsState()
    val cityName by mapViewModel.cityName.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
                Text(
                    text = cityName ?: "",
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(top = 8.dp)
                )

            OutlinedButton(
                onClick = onButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}
