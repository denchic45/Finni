package com.hackathon.finni.core.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
class IosNetworkObserver : NetworkObserver {

    private val _status = MutableStateFlow(NetworkStatus.DISCONNECTED)
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    private val monitor = nw_path_monitor_create()

    init {
        nw_path_monitor_set_update_handler(monitor) { path ->
            val isConnected = nw_path_get_status(path) == nw_path_status_satisfied
            _status.value = if (isConnected) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
    }

    fun stop() {
        nw_path_monitor_cancel(monitor)
    }
}
