package com.hackathon.finni.features.game_ui_showcase

import androidx.compose.runtime.Composable
import com.hackathon.finni.features.main.MainScreen

/**
 * Игровой экран витрины, отображающий главный экран игры «Дом Финни»
 * в строгом соответствии с дизайн-референсом.
 */
@Composable
fun GameUiShowcaseScreen(
    onBack: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    MainScreen(
        onSettingsClick = onSettingsClick
    )
}
