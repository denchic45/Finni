package com.hackathon.finni.core.network

import com.hackathon.finni.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket

class DesktopNetworkObserver(
    scope: ApplicationScope
) : NetworkObserver {

    private val _status = MutableStateFlow(NetworkStatus.CONNECTED)
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                _status.value = if (isHostReachable()) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
                delay(4000) // Проверка каждые 4 секунды
            }
        }
    }

    private fun isHostReachable(): Boolean {
        return try {
            Socket().use { socket ->
                // Пинг DNS Cloudflare на порт 53 с коротким таймаутом
                socket.connect(InetSocketAddress("1.1.1.1", 53), 1500)
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
