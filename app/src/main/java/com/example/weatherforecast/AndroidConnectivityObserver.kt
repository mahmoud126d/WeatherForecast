package com.example.weatherforecast


import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidConnectivityObserver(
    private val context: Context
): ConnectivityObserver {

    private val _connectivityManager = context.getSystemService<ConnectivityManager>()!!

    // Function to get the initial network state
    private fun getCurrentNetworkState(): Boolean {
        val network = _connectivityManager.activeNetwork
        val capabilities = _connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    }

    override val isConnected: Flow<Boolean>
        get() = callbackFlow {
            // Emit the initial connectivity state
            trySend(getCurrentNetworkState())

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val connected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    trySend(connected)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    trySend(false)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    trySend(false)
                }

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    trySend(true)
                }
            }

            _connectivityManager.registerDefaultNetworkCallback(callback)

            awaitClose {
                _connectivityManager.unregisterNetworkCallback(callback)
            }
        }
}