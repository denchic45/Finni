package com.hackathon.finni.core.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class AppLifecycleObserver : DefaultLifecycleObserver {

    private val _state = MutableStateFlow(AppLifecycleState.BACKGROUND)
    actual val state: StateFlow<AppLifecycleState> = _state.asStateFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        _state.value = AppLifecycleState.FOREGROUND
    }

    override fun onStop(owner: LifecycleOwner) {
        _state.value = AppLifecycleState.BACKGROUND
    }
}
