package com.hackathon.finni.core.lifecycle

import kotlinx.coroutines.flow.StateFlow

enum class AppLifecycleState {
    FOREGROUND, BACKGROUND
}

expect class AppLifecycleObserver {
    val state: StateFlow<AppLifecycleState>
}
