package com.hackathon.finni.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hackathon.finni.core.ui.components.game.PetMood
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.mood_happy
import com.hackathon.finni.resources.mood_neutral
import com.hackathon.finni.resources.mood_sad
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Возвращает соответствующий ресурс изображения для настроения питомца [PetMood].
 */
fun PetMood.drawableRes(): DrawableResource = when (this) {
    PetMood.Happy -> Res.drawable.mood_happy
    PetMood.Neutral -> Res.drawable.mood_neutral
    PetMood.Sad -> Res.drawable.mood_sad
}

/**
 * Игровой компонент индикатора настроения питомца (MoodIndicator):
 * - Отображает 3D-смайлик в шоколадной кольцевой рамке ([mood_happy.png], [mood_neutral.png], [mood_sad.png])
 * - Плавное переключение состояний с помощью классической анимации [Crossfade]
 * - Опциональная поддержка нажатий через [onClick]
 *
 * @param mood Текущее настроение питомца ([PetMood.Happy], [PetMood.Neutral], [PetMood.Sad])
 * @param modifier Модификатор контейнера
 * @param size Размер бейджа (по умолчанию 54.dp)
 * @param animationDurationMillis Длительность анимации crossfade в миллисекундах (по умолчанию 350 мс)
 * @param onClick Опциональный обработчик нажатия
 */
@Composable
fun MoodIndicator(
    mood: PetMood,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    animationDurationMillis: Int = 300,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        OverlayCrossfade(
            targetState = mood,
            animationDurationMillis = animationDurationMillis,
            modifier = Modifier.fillMaxSize()
        ) { targetMood ->
            Image(
                painter = painterResource(targetMood.drawableRes()),
                contentDescription = "Настроение: ${targetMood.name}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Превью индикатора настроения с интерактивным переключением для Compose Preview.
 */
@Preview
@Composable
fun MoodIndicatorPreview() {
    var currentMood by remember { mutableStateOf(PetMood.Happy) }

    Box(
        modifier = Modifier
            .background(Color(0xFFE8D7B8), RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Интерактивный бейдж, переключающий настроение по кругу
            MoodIndicator(
                mood = currentMood,
                size = 64.dp,
                onClick = {
                    currentMood = when (currentMood) {
                        PetMood.Happy -> PetMood.Neutral
                        PetMood.Neutral -> PetMood.Sad
                        PetMood.Sad -> PetMood.Happy
                    }
                }
            )

            // Статичные варианты всех трех состояний
            MoodIndicator(mood = PetMood.Happy, size = 48.dp)
            MoodIndicator(mood = PetMood.Neutral, size = 48.dp)
            MoodIndicator(mood = PetMood.Sad, size = 48.dp)
        }
    }
}
