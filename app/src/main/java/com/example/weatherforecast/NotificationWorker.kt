package com.example.weatherforecast

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.model.toCurrentWeather
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.CurrentWeatherRepository
import com.example.weatherforecast.repository.CurrentWeatherRepositoryImpl
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.repository.SettingsRepository
import com.example.weatherforecast.utils.Constants
import com.example.weatherforecast.utils.Response
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val TAG = "NotificationWorker"




class NotificationWorker(
    val context: Context,
    workerParams: WorkerParameters,
) :
    Worker(context, workerParams) {

    private val _currentWeather = MutableStateFlow<Response>(Response.Loading)
    val currentWeather: StateFlow<Response> = _currentWeather.asStateFlow()

    private var repo : CurrentWeatherRepository= CurrentWeatherRepositoryImpl.getInstance(
        CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService),
        WeatherLocalDataSourceImp(
            WeatherDataBase.getInstance(context).getWeatherDao()
        )
    )
    private val locationManager = LocationManager(context)
    private var locationRepository = LocationRepository(locationManager)


    init {
        locationRepository.startLocationUpdates()
    }
    override fun doWork(): Result {
        return try {
            val long = inputData.getDouble("KEY_LONG",0.0)
            val lat = inputData.getDouble("KEY_LAT",0.0)
            runBlocking {
                repo.getCurrentWeather(
                    lat = lat,
                    lon = long,
                    unit = "metric",
                    lang = "en",
                    appId = Constants.API_KEY
                )
                    .catch { ex ->
                        Log.d(TAG, "doWork: ${ex.message}")
                        _currentWeather.value = Response.Failure(ex)
                        showNotification(context, _currentWeather.value.toString())
                    }
                    .collect { response ->
                        val currentWeatherData = response.toCurrentWeather()
                        Log.d(TAG, "doWork: $currentWeatherData")
                        showNotification(context, currentWeatherData.city)
                        _currentWeather.value = Response.Success(currentWeatherData)
                    }
            }
            Result.success()
        } catch (ex: Exception) {
            _currentWeather.value = Response.Failure(ex)
            showNotification(context, _currentWeather.value.toString())
            Result.failure()
        }
    }

    @SuppressLint("ServiceCast")
    private fun showNotification(context: Context, message: String) {
        val channelId = "alarm_channel"
        val channelName = "Alarm Notifications"

        // Create notification manager
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Alarm Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open MainActivity when the notification is clicked
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Show notification
        notificationManager.notify(1001, notification)
    }
}