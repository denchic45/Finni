package com.hackathon.finni.core.presentation.handlers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.hackathon.finni.core.LocalBackDispatcher

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    val dispatcher = LocalBackDispatcher.current

    DisposableEffect(enabled, onBack) {
        if (enabled) {
            dispatcher.register(onBack)
        }
        onDispose {
            if (enabled) {
                dispatcher.unregister(onBack)
            }
        }
    }
}