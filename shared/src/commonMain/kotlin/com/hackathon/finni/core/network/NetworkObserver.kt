package com.hackathon.finni.core.network

import kotlinx.coroutines.flow.StateFlow

enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED
}

interface NetworkObserver {
    val status: StateFlow<NetworkStatus>
    val isConnected: Boolean
        get() = status.value == NetworkStatus.CONNECTED
}
