package com.example.weatherforecast.utils


import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

object AndroidConnectivityObserver : ConnectivityObserver {

    private var contextRef: WeakReference<Context>? = null
    private val _connectivityManager: ConnectivityManager?
        get() = contextRef?.get()?.getSystemService(ConnectivityManager::class.java)

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: Flow<Boolean> get() = _isConnected.asStateFlow()

    @Volatile
    private var isRegistered = false

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            _isConnected.value = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        override fun onUnavailable() {
            _isConnected.value = false
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
        }

        override fun onAvailable(network: Network) {
            _isConnected.value = true
        }
    }

    fun initialize(appContext: Context) {
        if (contextRef == null || contextRef?.get() == null) {
            contextRef = WeakReference(appContext.applicationContext)
            registerNetworkCallback()
        }
    }

    private fun registerNetworkCallback() {
        val connectivityManager = _connectivityManager ?: return
        if (!isRegistered) {
            _isConnected.value = getCurrentNetworkState()
            connectivityManager.registerDefaultNetworkCallback(callback)
            isRegistered = true
        }
    }


    private fun getCurrentNetworkState(): Boolean {
        val connectivityManager = _connectivityManager ?: return false
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    }
}
