package com.hackathon.finni.core.ui.components.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hackathon.finni.core.ui.components.HungerIndicator as CoreHungerIndicator

/**
 * Делегат к [com.hackathon.finni.core.ui.components.HungerIndicator]
 * для обратной совместимости с существующими экранами (например, GameUiShowcaseScreen).
 */
@Composable
fun HungerRadialGauge(
    hungerValue: Int,
    maxHunger: Int = 6,
    segmentsCount: Int = 6,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    onBowlClick: (() -> Unit)? = null
) {
    CoreHungerIndicator(
        hunger = hungerValue,
        modifier = modifier,
        size = size,
        onClick = onBowlClick
    )
}

/**
 * Синоним для [com.hackathon.finni.core.ui.components.HungerIndicator] в пакете game.
 */
@Composable
fun HungerIndicator(
    hunger: Int,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    animationDurationMillis: Int = 350,
    onClick: (() -> Unit)? = null
) {
    CoreHungerIndicator(
        hunger = hunger,
        modifier = modifier,
        size = size,
        animationDurationMillis = animationDurationMillis,
        onClick = onClick
    )
}
