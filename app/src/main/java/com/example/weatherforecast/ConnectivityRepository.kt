package com.example.weatherforecast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectivityRepository(private val androidConnectivityObserver:AndroidConnectivityObserver) {


    private val _isConnectedState = MutableStateFlow(false)
    val isConnectedState: StateFlow<Boolean> = _isConnectedState.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            androidConnectivityObserver.isConnected.collect { status ->
                _isConnectedState.value = status
            }
        }
    }

}