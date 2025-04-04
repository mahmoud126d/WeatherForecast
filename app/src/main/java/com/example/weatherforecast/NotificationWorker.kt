package com.example.weatherforecast

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.weatherforecast.db.WeatherDataBase
import com.example.weatherforecast.db.WeatherLocalDataSourceImp
import com.example.weatherforecast.model.toCurrentWeather
import com.example.weatherforecast.model.weatherData
import com.example.weatherforecast.network.CurrentWeatherRemoteDataSourceImpl
import com.example.weatherforecast.network.RetrofitHelper
import com.example.weatherforecast.repository.WeatherRepository
import com.example.weatherforecast.repository.WeatherRepositoryImpl
import com.example.weatherforecast.repository.LocationRepository
import com.example.weatherforecast.utils.AppIcons
import com.example.weatherforecast.utils.Constants
import com.example.weatherforecast.utils.LocationManager
import com.example.weatherforecast.utils.Response
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.runBlocking

private const val TAG = "NotificationWorker"

class NotificationWorker(
    val context: Context,
    workerParams: WorkerParameters,
) :
    Worker(context, workerParams) {

    private val _currentWeather = MutableStateFlow<Response>(Response.Loading)

    private var repo: WeatherRepository = WeatherRepositoryImpl.getInstance(
        CurrentWeatherRemoteDataSourceImpl(RetrofitHelper.retrofitService),
        WeatherLocalDataSourceImp(
            WeatherDataBase.getInstance(context).getWeatherDao()
        )
    )
    private val locationManager = LocationManager(context)
    private var locationRepository = LocationRepository(locationManager)

    val long = inputData.getDouble("KEY_LONG", 0.0)
    val lat = inputData.getDouble("KEY_LAT", 0.0)
    val date = inputData.getString("KEY_DATE")
    val time = inputData.getString("KEY_TIME")

    init {
        locationRepository.startLocationUpdates()
    }

    @SuppressLint("RestrictedApi")
    override fun doWork(): Result {
        runBlocking {
            try {
                repo.getCurrentWeather(
                    lat = lat,
                    lon = long,
                    unit = "metric",
                    lang = "en",
                    appId = Constants.API_KEY
                )
                    .catch {
                        showNotification(
                            context,
                            "",
                            temp = -900.0,
                            weatherDescription = "",
                        )
                    }
                    .collect { response ->
                        val currentWeatherData = response.toCurrentWeather()
                        showNotification(
                            context,
                            city = currentWeatherData.city,
                            temp = currentWeatherData.temperature,
                            weatherDescription = currentWeatherData.description
                        )
                        _currentWeather.value = Response.Success(currentWeatherData)
                    }

                if (date != null && time != null) {
                    repo.deleteAlert(date, time)
                }

                return@runBlocking Result.success()
            } catch (ex: Exception) {
                showNotification(
                    context,
                    "",
                    temp = -900.0,
                    weatherDescription = "",
                )
                if (date != null && time != null) {
                    repo.deleteAlert(date, time)
                }
                return@runBlocking Result.Failure()
            }

        }
        return Result.Success()
    }

    @SuppressLint("ServiceCast")
    private fun showNotification(
        context: Context,
        city: String,
        temp: Double,
        weatherDescription: String
    ) {
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

        // Create a large icon for the notification
        val largeIcon = BitmapFactory.decodeResource(
            context.resources, AppIcons.getWeatherIcon(
                weatherData.icon
            )
        )

        // Create the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.cloudandsun)
            .setLargeIcon(largeIcon)
            .setContentTitle( if(temp==-900.0) context.getString(R.string.check_your_internet_connection) else  "${temp.toInt()}° in $city")
            .setContentText(context.getString(R.string.see_full_forecast, weatherDescription))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

            .setContentIntent(pendingIntent)
            .setColor(context.getColor(R.color.purple_500))
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Show notification
        notificationManager.notify(1001, notification.build())
    }
}