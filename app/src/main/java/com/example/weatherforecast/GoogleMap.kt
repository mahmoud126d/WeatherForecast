package com.example.weatherforecast

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun  GoogleMap (){
    Column(
    modifier=Modifier.fillMaxSize().wrapContentSize()
    ) {
        GoogleMapScreen()
    }
}

@Preview(showSystemUi = true, device = Devices.PIXEL_4)
@Composable
fun GoogleMapScreen() {
    val egypt = LatLng(10.0, 10.0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(egypt, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = rememberMarkerState(position = egypt),
            title = "Cairo",
            snippet = "Capital of Egypt",
            onClick = {
                // Action to perform on marker click
                Log.d("GoogleMapScreen", "Marker clicked")
                // Optionally, you can update a state variable or show a dialog here.
                true // Return true to indicate the event is consumed.
            }
        )
    }
}
