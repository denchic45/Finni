package com.hackathon.finni.core.ui.components.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.GameTheme

enum class PetMood {
    Happy,
    Neutral,
    Sad
}

/**
 * Индикатор настроения питомца из референса:
 * Деревянно-бронзовый круглый жетон с выразительным смайликом.
 */
@Composable
fun MoodIndicatorBadge(
    mood: PetMood,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    onClick: (() -> Unit)? = null
) {
    val colors = GameTheme.colors

    val (faceBgColor, emoji) = when (mood) {
        PetMood.Happy -> Color(0xFFFFB300) to "😊"
        PetMood.Neutral -> Color(0xFFFFCA28) to "😐"
        PetMood.Sad -> Color(0xFF90A4AE) to "😢"
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 4.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF8D5325),
                        Color(0xFF5C2D0E),
                        Color(0xFF381A07)
                    )
                )
            )
            .border(
                width = 3.dp,
                color = colors.moodFrame,
                shape = CircleShape
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        // Внутренний диск смайлика
        Box(
            modifier = Modifier
                .size(size * 0.76f)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            faceBgColor.copy(alpha = 0.95f),
                            faceBgColor.copy(alpha = 0.75f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = Color(0xFF4A250E).copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = emoji,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "mood_emoji"
            ) { targetEmoji ->
                Text(
                    text = targetEmoji,
                    fontSize = (size.value * 0.42f).sp
                )
            }
        }
    }
}
