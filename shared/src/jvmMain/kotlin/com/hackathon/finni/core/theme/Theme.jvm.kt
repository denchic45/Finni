package com.hackathon.finni.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun platformColorScheme(
    isDarkTheme: Boolean,
    dynamicColor: Boolean,
    lightColorScheme: ColorScheme,
    darkColorScheme: ColorScheme
): ColorScheme = if (isDarkTheme) darkColorScheme else lightColorScheme
