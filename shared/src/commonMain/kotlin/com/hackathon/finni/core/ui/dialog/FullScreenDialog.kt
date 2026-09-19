package com.hackathon.finni.core.ui.dialog

import androidx.compose.runtime.Composable

@Composable
expect fun FullScreenDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
)