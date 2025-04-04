package com.example.weatherforecast.alert.view

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.db.DataStoreManager
import com.example.weatherforecast.utils.LanguageHelper
import com.example.weatherforecast.utils.LocationManager
import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weatherforecast.R
import com.example.weatherforecast.alert.viewmodel.AlertViewModel
import com.example.weatherforecast.alert.viewmodel.AlertViewModelFactory
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.WeatherRepositoryImpl
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

private lateinit var alertViewModel: AlertViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AlarmsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val factory = AlertViewModelFactory(
        LocationRepository(LocationManager(context)),
        application,
        WeatherRepositoryImpl.getInstance(
            CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService),
            WeatherLocalDataSourceImp(
                WeatherDataBase.getInstance(context).getWeatherDao()
            )
        ),
        SettingsRepository(
            DataStoreManager(context.applicationContext),
            LanguageHelper
        )
    )
    alertViewModel = viewModel(factory = factory)

    val alertList = alertViewModel.alertList.collectAsState()
    var isBottomSheetVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        alertViewModel.toastEvent.collect { message ->
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                    actionLabel = "Undo"
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        alertViewModel.undoDelete()
                    }

                    SnackbarResult.Dismissed -> {

                    }
                }
            }

        }
    }


    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
       onResult = {

       }
    )

    val onFabClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = NotificationManagerCompat.from(context)
            val hasPermission = notificationManager.areNotificationsEnabled()
            if (!hasPermission) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        isBottomSheetVisible = true
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = colorResource(id = R.color.background_color),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onFabClick() },
                containerColor = colorResource(R.color.purple_500),
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Add Alert",
                    tint = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            if (alertList.value.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Animation()
                }
            } else {
                AlertColumn(alerts = alertList.value)
            }
            BottomSheet(
                isVisible = isBottomSheetVisible,
                onDismiss = { isBottomSheetVisible = false }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    isVisible: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf("") }

    var showTimePicker by remember { mutableStateOf(false) }

    var selectedTime by remember { mutableStateOf("") }

    val context = LocalContext.current

    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = false
    )

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis


    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today + 86400000,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= today
            }
        }
    )

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = sheetState,
            containerColor = colorResource(R.color.purple_alpha_70)
        ) {
            Column(
                Modifier
                    .height(300.dp)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Create Weather Alert", fontSize = 20.sp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.date), modifier = Modifier.weight(0.3f))
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(0.7f)
                    ) {
                        Text(if (selectedDate.isEmpty()) stringResource(R.string.select_date) else selectedDate)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.time), modifier = Modifier.weight(0.3f))
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(0.7f)
                    ) {
                        Text(if (selectedTime.isEmpty()) stringResource(R.string.select_time) else selectedTime)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                                scope.launch {
                                    val alert = AlertData(
                                        "Cairo",
                                        selectedDate,
                                        selectedTime,
                                        0.0,
                                        0.0,
                                        System.currentTimeMillis(),
                                        ""
                                    )
                                    alertViewModel.scheduleNotification(alert)

                                    onDismiss()
                                }
                            }
                        },
                        enabled = selectedDate.isNotEmpty() && selectedTime.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.save))
                    }

                    Button(
                        onClick = { onDismiss() }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
            var selectedLocalDate by remember { mutableStateOf<LocalDate?>(null) }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                                    selectedDate = formatter.format(Date(millis))

                                    selectedLocalDate = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(stringResource(R.string.select_a_date))
                            }
                        }
                    )
                }
            }

            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = { Text(stringResource(R.string.select_time)) },
                    text = {
                        TimePicker(state = timePickerState)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val selectedHour = timePickerState.hour
                                val selectedMinute = timePickerState.minute
                                val selectedLocalTime = LocalTime.of(selectedHour, selectedMinute)
                                val currentDate = LocalDate.now()
                                val currentLocalTime = LocalTime.now()

                                if (selectedLocalDate == currentDate && selectedLocalTime.isBefore(currentLocalTime)) {
                                    Toast.makeText(context,
                                        context.getString(R.string.selected_time_is_in_the_past), Toast.LENGTH_SHORT).show()
                                } else {
                                    val timeString = String.format(
                                        "%02d:%02d %s",
                                        if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour,
                                        selectedMinute,
                                        if (selectedHour >= 12) context.getString(R.string.pm) else context.getString(R.string.am)
                                    )
                                    selectedTime = timeString
                                    showTimePicker = false
                                }
                            }
                        ) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }


        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AlertColumn(modifier: Modifier = Modifier, alerts: List<AlertData>) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = alerts,
            key = { _, alert -> "${alert.date}|${alert.time}" }
        ) { index, item ->
            val dismissState = rememberDismissState(
                confirmStateChange = { dismissValue ->
                    if (dismissValue == DismissValue.DismissedToStart) {
                        alertViewModel.cancelNotification(item.date,item.time)
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismiss(
                state = dismissState,
                directions = setOf(DismissDirection.EndToStart),
                background = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Red)
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                dismissContent = {
                    AlertItem(
                        date = item.date,
                        time = item.time
                    )
                }
            )
        }
    }
}

@Composable
fun AlertItem(modifier: Modifier = Modifier, date: String, time: String) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(date, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(time, fontSize = 14.sp, color = Color.Gray)
            }
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Alert",
                tint = colorResource(R.color.purple_500),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun AlertPreview(modifier: Modifier = Modifier) {
    AlertItem(
        date = "12/12/1997",
        time = "4:00Pm"
    )
}

@Composable
fun Animation(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.alert))
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