package com.hackathon.finni.core.ui.dialog


import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

@Composable
actual fun FullScreenDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogWindow = getDialogWindow()
        SideEffect {
            dialogWindow?.let { window ->
                val styleId = window.context.resources.getIdentifier(
                    "FadeDialog",
                    "style",
                    window.context.packageName
                )
                window.setWindowAnimations(styleId)
            }
        }
        content()
    }
}

@ReadOnlyComposable
@Composable
fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window