package com.example.weatherforecast.alarms.view

import android.annotation.SuppressLint
import android.os.Build
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecast.LocationManager
import com.example.weatherforecast.NotificationScheduler
import com.example.weatherforecast.R
import com.example.weatherforecast.alarms.viewmodel.AlarmsViewModel
import com.example.weatherforecast.alarms.viewmodel.AlarmsViewModelFactory
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl
import com.example.weatherforecast.repository.LocationRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private lateinit var alarmsViewModel: AlarmsViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AlarmsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val factory = AlarmsViewModelFactory(
        CurrentWeatherRepositoryImpl.getInstance(
            CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService),
            WeatherLocalDataSourceImp(
                WeatherDataBase.getInstance(context).getWeatherDao()
            )
        ),
        LocationRepository(LocationManager(context)),
    )
    alarmsViewModel = viewModel(factory = factory)

    val alertList = alarmsViewModel.alertList.collectAsState()
    var isBottomSheetVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colorResource(id = R.color.background_color),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Fixed: Added onClick handler to show bottom sheet
                    isBottomSheetVisible = true
                },
                containerColor = colorResource(R.color.purple_500),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Add Alert", tint = Color.White)
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
                    Text(stringResource(R.string.no_alerts_set_tap_to_add_a_weather_alert))
                }
            } else {
                AlertColumn(
                    alerts = alertList.value
                )
            }
            BottomSheet(
                isVisible = isBottomSheetVisible,
                onDismiss = { isBottomSheetVisible = false }
            )
        }
    }
}

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

    // State to control the visibility of the date picker dialog
    var showDatePicker by remember { mutableStateOf(false) }

    // State to store the selected date
    var selectedDate by remember { mutableStateOf("") }

    // States for date and time pickers
    var showTimePicker by remember { mutableStateOf(false) }

    // States to store selected date and time
    var selectedTime by remember { mutableStateOf("") }

    // Time picker state
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = false
    )

    // Get today's date in milliseconds
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val context = LocalContext.current

    // Date picker state with start date set to today
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today + 86400000,
        selectableDates = object : SelectableDates {
            // Override to only allow dates from today onwards
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
                            // Only save if both date and time are selected
                            if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                                scope.launch {
                                    // Get current location (simplified for this example)
                                    //val locationRepository = alarmsViewModel.
                                   // val location = locationRepository.getCurrentLocation()

                                    saveAlert(
                                        city =  "Current Location",
                                        long =  0.0,
                                        lat =  0.0,
                                        date = selectedDate,
                                        time = selectedTime
                                    )

                                    NotificationScheduler.scheduleNotification(
                                        long =  0.0,
                                        lat =  0.0,
                                        date = selectedDate,
                                        time = selectedTime,
                                        context = context
                                    )

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

            // Date Picker Dialog
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // Convert milliseconds to formatted date string
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val formatter =
                                        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                                    selectedDate = formatter.format(Date(millis))
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
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

            // Time Picker Dialog
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
                                // Format time
                                val timeString = String.format(
                                    "%02d:%02d %s",
                                    if (timePickerState.hour > 12) timePickerState.hour - 12 else if (timePickerState.hour == 0) 12 else timePickerState.hour,
                                    timePickerState.minute,
                                    if (timePickerState.hour >= 12) "PM" else "AM"
                                )

                                selectedTime = timeString
                                showTimePicker = false
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

@RequiresApi(Build.VERSION_CODES.O)
private fun saveAlert(city: String, long: Double, lat: Double, date: String, time: String) {
    alarmsViewModel.saveAlert(city, long, lat, date, time)
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
            key = { _, alert -> "${alert.date}|${alert.time}" } // Composite key
        ) { index, item ->
            val dismissState = rememberDismissState(
                confirmStateChange = { dismissValue ->
                    if (dismissValue == DismissValue.DismissedToStart) {
                        alarmsViewModel.deleteFromAlerts(item)
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
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                dismissContent = {
                    AlertItem(
                        city = item.city,
                        date = item.date,
                        time = item.time
                    )
                }
            )
        }
    }
}
@Composable
fun AlertItem(modifier: Modifier = Modifier, city: String, date: String, time: String) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(city, fontSize = 18.sp)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(date, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(time, fontSize = 14.sp, color = Color.Gray)
            }
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Alert",
                tint = colorResource(R.color.purple_500)
            )
        }
    }
}
