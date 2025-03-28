package com.example.weatherforecast.alarms.view

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.weatherforecast.LocationManager
import com.example.weatherforecast.NotificationScheduler
import com.example.weatherforecast.alarms.viewmodel.AlarmsViewModel
import com.example.weatherforecast.alarms.viewmodel.AlarmsViewModelFactory
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.favorites.view.WeatherItem
import com.example.weatherforecast.model.AlertData
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private lateinit var alarmsViewModel: AlarmsViewModel

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        AlertColumn(
            alerts = alertList.value
        )
        BottomSheet()
    }
}


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(modifier: Modifier = Modifier) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var isBottomSheetVisible by remember { mutableStateOf(false) }
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

    if (isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                isBottomSheetVisible = false
            },
            sheetState = sheetState,
            containerColor = Color.Cyan
        ) {
            Column(
                Modifier
                    .height(250.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Text("Date : ")
                    // Button to open the date picker
                    Button(onClick = { showDatePicker = true }) {
                        Text("Select Date")
                    }
                    Text(selectedDate, fontSize = 30.sp)
                }
                Spacer(Modifier.height(20.dp))

                Row {
                    Text("Time : ")
                    Button(onClick = { showTimePicker = true }) {
                        Text("Select Time")
                    }
                    Text(selectedTime, fontSize = 30.sp)
                }
                Row {
                    Button({
                        saveAlert("Suez",12.0,90.0,selectedDate,selectedTime)
                        NotificationScheduler.scheduleNotification(
                            long = 10.0,
                            lat = 10.0,
                            date = selectedDate,
                            time = selectedTime,
                            context = context
                        )
                    }) {
                        Text("Save")
                    }
                    Button({}) {
                        Text("Cancel")
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
                                    Text("Select a Date")
                                }
                            }
                        )
                    }
                }
                // Time Picker Dialog
                if (showTimePicker) {
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        title = { Text("Select Time") },
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

                                    // Update selected date time
                                    selectedTime = if (selectedTime.contains("/")) {
                                        "${selectedTime.split(" ")[0]} $timeString"
                                    } else {
                                        timeString
                                    }

                                    showTimePicker = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                isBottomSheetVisible = !isBottomSheetVisible
            }
        ) {
            Text(text = "Toggle Bottom Sheet")
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
    LazyColumn {
        items(alerts.size) { index ->
            val item = alerts[index]
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
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color.White)
//                            .padding(16.dp),
//                        contentAlignment = Alignment.CenterEnd
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Delete,
//                            contentDescription = "Delete",
//                            tint = Color.White,
//                            modifier = Modifier.size(40.dp)
//                        )
//                    }
                },
                dismissContent = {
                    AlertItem(
                        city = alerts[index].city,
                        date = alerts[index].date,
                        time = alerts[index].time
                    )
                }
            )

        }
    }
}

@Composable
fun AlertItem(modifier: Modifier = Modifier, city: String, date: String, time: String) {
    Card(
        modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(city)
            Column {
                Text(date)
                Text(time)
            }
            Icon(imageVector = Icons.Default.Notifications, contentDescription = "")
        }
    }
}

@Composable
fun BottomSheetPreview(modifier: Modifier = Modifier) {
    BottomSheet()
}


