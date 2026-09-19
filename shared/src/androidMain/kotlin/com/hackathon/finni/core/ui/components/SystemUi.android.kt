package com.hackathon.finni.core.ui.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun SystemUiSync(interactor: SystemUiInteractor) {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window ?: return
    val controller = remember(window, view) { WindowCompat.getInsetsController(window, view) }

    val isDarkIcons by interactor.state.collectAsState()

    LaunchedEffect(isDarkIcons) {
        controller.isAppearanceLightStatusBars = isDarkIcons
    }
}