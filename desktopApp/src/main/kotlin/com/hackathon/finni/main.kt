package com.hackathon.finni

import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hackathon.finni.core.DesktopBackDispatcher

fun main() = application {
    val backDispatcher = remember { DesktopBackDispatcher() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Finni",
        onKeyEvent = { event ->
            // Перехватываем только момент отпускания клавиши (KeyUp),
            // чтобы событие не срабатывало многократно при удержании Escape
            if (event.type == KeyEventType.KeyUp && event.key == Key.Escape) {
                val handled = backDispatcher.onBackPressed()
                handled // true - событие потреблено, false - пропустить дальше
            } else {
                false
            }
        }
    ) {
        App()
    }
}