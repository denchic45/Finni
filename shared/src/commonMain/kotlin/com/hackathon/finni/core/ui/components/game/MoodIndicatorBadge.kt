package com.hackathon.finni.core.ui.components.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hackathon.finni.core.ui.components.MoodIndicator as CoreMoodIndicator

enum class PetMood {
    Happy,
    Neutral,
    Sad
}

/**
 * Делегат к [com.hackathon.finni.core.ui.components.MoodIndicator]
 * для обратной совместимости с существующими импортами в проекте.
 */
@Composable
fun MoodIndicatorBadge(
    mood: PetMood,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    onClick: (() -> Unit)? = null
) {
    CoreMoodIndicator(
        mood = mood,
        modifier = modifier,
        size = size,
        onClick = onClick
    )
}

/**
 * Синоним для [com.hackathon.finni.core.ui.components.MoodIndicator] в пакете game.
 */
@Composable
fun MoodIndicator(
    mood: PetMood,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    animationDurationMillis: Int = 350,
    onClick: (() -> Unit)? = null
) {
    CoreMoodIndicator(
        mood = mood,
        modifier = modifier,
        size = size,
        animationDurationMillis = animationDurationMillis,
        onClick = onClick
    )
}
