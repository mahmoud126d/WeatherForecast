package com.example.weatherforecast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherforecast.utils.Constants
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(true) {
        delay(3000)
        navController.navigate(Constants.HOME_SCREEN) {
            popUpTo(Constants.SPLASH_SCREEN) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_color)),
        contentAlignment = Alignment.Center
    ) {
        LoadingAnimation()
    }
}

@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.splash))
    val animationState by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = 100
    )
    LottieAnimation(
        composition = composition,
        progress = { animationState },
        modifier = modifier.fillMaxSize().wrapContentSize()
    )

}