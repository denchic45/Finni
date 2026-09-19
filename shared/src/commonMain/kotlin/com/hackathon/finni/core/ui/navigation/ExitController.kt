package com.hackathon.finni.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class ExitController {
    var isExiting by mutableStateOf(false)
        internal set

    var hasHandler by mutableStateOf(false)
        internal set

    internal var onFinished: (() -> Unit)? = null

    fun finishExit() {
        onFinished?.invoke()
    }
}

val LocalExitController = staticCompositionLocalOf<ExitController?> { null }

/**
 * Перехватывает удаление экрана из backstack и вызывает suspend-функцию перед размонтированием.
 */
@Composable
fun SuspendExitEffect(onExit: suspend () -> Unit) {
    val controller = LocalExitController.current ?: return

    DisposableEffect(controller) {
        controller.hasHandler = true
        onDispose { controller.hasHandler = false }
    }

    LaunchedEffect(controller.isExiting) {
        if (controller.isExiting) {
            try {
                onExit()
            } finally {
                controller.finishExit()
            }
        }
    }
}