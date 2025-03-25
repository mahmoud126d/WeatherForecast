package com.example.weatherforecast.map.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.weatherforecast.DataStoreManager
import com.example.weatherforecast.LanguageChangeHelper
import com.example.weatherforecast.map.viewmodel.MapViewModel
import com.example.weatherforecast.map.viewmodel.MapViewModelFactory
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import com.example.weatherforecast.utils.Constants
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.properties.Delegates

var long by Delegates.notNull<Double>()
var lat by Delegates.notNull<Double>()

@Composable
fun MapScreen(modifier: Modifier = Modifier, navController: NavHostController) {

    val context = LocalContext.current
    val locationManager = com.example.weatherforecast.LocationManager(context)
    val locationRepository = LocationRepository(locationManager)
    val factory = MapViewModelFactory(
        LocationRepository(locationManager),
        SettingsRepository(
            DataStoreManager(context.applicationContext),
            LanguageChangeHelper(context)
        )
    )
    val mapViewModel: MapViewModel = viewModel(factory = factory)
    Box(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        GoogleMapScreen(mapViewModel)
        LocationSelectionCard(
            navController = navController,
            mapViewModel = mapViewModel,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun GoogleMapScreen(mapViewModel: MapViewModel) {

    // Use mutableStateOf with a unique key for each marker position
    var markerId by remember { mutableStateOf(0) }
    var markerPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            // Update marker position AND increment ID to force recomposition
            markerPosition = latLng
            markerId++  // This forces the marker to be recreated

            mapViewModel.askForAddress(latLng.latitude, latLng.longitude)
            long =  latLng.longitude
            lat =  latLng.latitude
            Log.d("TAG", "GoogleMapScreen: Marker placed at $latLng (ID: $markerId)")
        }
    ) {
        // Use the key parameter with markerId to force the Marker to be recreated
        key(markerId) {
            Marker(
                state = MarkerState(position = markerPosition),
                title = "Selected Location",
                snippet = "Location #$markerId"
            )
        }
    }
//    address?.let {
//        //Text(text = "Latitude: ${it.latitude}, Longitude: ${it.longitude}")
//        Log.d("TAG", "address: ${it.getAddressLine(0)}")
//    }
}


@Composable
fun LocationSelectionCard(
    modifier: Modifier = Modifier, // 👈 Added modifier parameter
    navController: NavController,
    mapViewModel: MapViewModel
) {
    val address by mapViewModel.address.collectAsState()
    val cityName by mapViewModel.cityName.collectAsState()

    Card(
        modifier = modifier // 👈 Use modifier here
            .fillMaxWidth()
            .height(160.dp)
            .background(Color.Cyan)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    navController.navigate(Constants.SETTINGS_SCREEN)
                    mapViewModel.saveLatitude(lat)
                    mapViewModel.saveLongitude(long)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Select Location")
            }
            cityName?.let {
                Text(
                    text = it,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}
