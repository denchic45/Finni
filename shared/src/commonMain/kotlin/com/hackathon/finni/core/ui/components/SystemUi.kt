package com.hackathon.finni.core.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@Composable
expect fun SystemUiSync(interactor: SystemUiInteractor)

class SystemUiInteractor {
    private val _state = MutableStateFlow(false)

    private var lastExecutionTime = 0L
    private val throttleInterval = 100L

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val state: StateFlow<Boolean> = _state.asStateFlow()

    @OptIn(ExperimentalTime::class)
    fun update(isDark: Boolean) {
        val currentTime = Clock.System.now().toEpochMilliseconds()

        if (currentTime - lastExecutionTime >= throttleInterval) {
            lastExecutionTime = currentTime
            _state.value = isDark
        }
    }

    fun reset() {
        update(true)
    }
}

@Composable
fun BindSystemAppearance(isDarkIcons: Boolean) {
    val interactor = LocalSystemUiInteractor.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isDarkTheme = isSystemInDarkTheme()

    DisposableEffect(lifecycleOwner, isDarkIcons) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    interactor.update(isDarkIcons)
                }

                Lifecycle.Event.ON_DESTROY -> {
                    interactor.update(!isDarkTheme)
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

val LocalSystemUiInteractor = staticCompositionLocalOf<SystemUiInteractor> {
    throw IllegalStateException("No status bar interactor")
}