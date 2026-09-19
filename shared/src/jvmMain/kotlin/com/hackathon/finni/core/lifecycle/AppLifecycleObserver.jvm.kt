package com.hackathon.finni.core.lifecycle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class AppLifecycleObserver {

    private val _state = MutableStateFlow(AppLifecycleState.FOREGROUND)
    actual val state: StateFlow<AppLifecycleState> = _state.asStateFlow()
}
