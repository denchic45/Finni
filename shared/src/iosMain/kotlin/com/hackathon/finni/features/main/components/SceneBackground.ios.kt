package com.hackathon.finni.features.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun SceneBackground(
    modifier: Modifier,
    modelPath: String,
    hdrPath: String
) {
    Box(
        modifier = modifier.background(Color(0xFFD9D9D9))
    )
}
