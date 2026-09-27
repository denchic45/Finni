package com.hackathon.finni.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.comfortaaFontFamily
import com.hackathon.finni.core.ui.components.game.GameTimePhase
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.time_day
import com.hackathon.finni.resources.time_evening
import com.hackathon.finni.resources.time_morning
import com.hackathon.finni.resources.time_night
import com.hackathon.finni.resources.time_plate
import com.hackathon.finni.resources.time_plate_docked
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Маппинг фазы суток в соответствующий ресурс иконки.
 */
fun gameTimeDrawableRes(phase: GameTimePhase): DrawableResource = when (phase) {
    GameTimePhase.Morning -> Res.drawable.time_morning
    GameTimePhase.Day -> Res.drawable.time_day
    GameTimePhase.Evening -> Res.drawable.time_evening
    GameTimePhase.Night -> Res.drawable.time_night
}

/**
 * Игровой компонент времени суток (TimeOfDayIndicator):
 * - Полупрозрачная пустая плашка с белой обводкой ([time_plate_docked] или [time_plate])
 * - 4 состояния времени суток: [GameTimePhase.Morning], [GameTimePhase.Day], [GameTimePhase.Evening], [GameTimePhase.Night]
 * - Плавная анимация смены фаз через [Crossfade]
 * - Поддержка тактильного отклика при клике (пружинное сжатие)
 *
 * @param phase Текущая фаза суток (Утро, День, Вечер, Ночь)
 * @param modifier Модификатор контейнера
 * @param height Высота индикатора (по умолчанию 48.dp)
 * @param isDocked Пристыкована ли плашка к правому краю экрана (false = симметричная капсула)
 * @param showPlate Отображать ли полупрозрачную подложку-плашку
 * @param animationDurationMillis Длительность анимации Crossfade в миллисекундах
 * @param onClick Обработчик нажатия (опционально)
 */
@Composable
fun TimeOfDayIndicator(
    phase: GameTimePhase,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    isDocked: Boolean = true,
    showPlate: Boolean = true,
    animationDurationMillis: Int = 300,
    onClick: (() -> Unit)? = null
) {
    // Соотношение сторон плашек:
    // time_plate_docked: 953 x 542 (~1.758f)
    // time_plate: 882 x 542 (~1.627f)
    val aspectRatio = if (!showPlate) 1.0f else if (isDocked) (953f / 542f) else (882f / 542f)

    Box(
        modifier = modifier
            .height(height)
            .aspectRatio(aspectRatio)
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
        // 1. Полупрозрачная пустая плашка бейджа
        if (showPlate) {
            val plateRes = if (isDocked) Res.drawable.time_plate_docked else Res.drawable.time_plate
            Image(
                painter = painterResource(plateRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // 2. Иконка времени суток: плавный Crossfade (старая картинка исчезает одновременно с появлением новой)
        val iconSize = height * 0.88f
        Box(
            modifier = Modifier.size(iconSize),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = phase,
                animationSpec = tween(durationMillis = animationDurationMillis),
                modifier = Modifier.fillMaxSize(),
                label = "time_of_day_crossfade"
            ) { currentPhase ->
                Image(
                    painter = painterResource(gameTimeDrawableRes(currentPhase)),
                    contentDescription = currentPhase.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

/**
 * Автономная иконка времени суток без плашки.
 */
@Composable
fun TimeOfDayIcon(
    phase: GameTimePhase,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    animationDurationMillis: Int = 350,
    onClick: (() -> Unit)? = null
) {
    TimeOfDayIndicator(
        phase = phase,
        modifier = modifier,
        height = size,
        showPlate = false,
        animationDurationMillis = animationDurationMillis,
        onClick = onClick
    )
}

/**
 * Превью компонента TimeOfDayIndicator.
 */
@Preview
@Composable
fun TimeOfDayIndicatorPreview() {
    var currentPhase by remember { mutableStateOf(GameTimePhase.Day) }

    Box(
        modifier = Modifier
            .background(Color(0xFF81D4FA), RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Нажми для переключения: ${currentPhase.title}",
                fontFamily = comfortaaFontFamily,
                color = Color(0xFF1E3050),
                fontSize = 14.sp
            )

            // 1. Интерактивная плашка (docked)
            TimeOfDayIndicator(
                phase = currentPhase,
                height = 54.dp,
                isDocked = true,
                onClick = {
                    currentPhase = when (currentPhase) {
                        GameTimePhase.Morning -> GameTimePhase.Day
                        GameTimePhase.Day -> GameTimePhase.Evening
                        GameTimePhase.Evening -> GameTimePhase.Night
                        GameTimePhase.Night -> GameTimePhase.Morning
                    }
                }
            )

            // 2. Все 4 фазы подряд (симметричная плашка)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameTimePhase.entries.forEach { phase ->
                    TimeOfDayIndicator(
                        phase = phase,
                        height = 44.dp,
                        isDocked = false
                    )
                }
            }

            // 3. Только иконки (без плашки)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameTimePhase.entries.forEach { phase ->
                    TimeOfDayIcon(
                        phase = phase,
                        size = 40.dp
                    )
                }
            }
        }
    }
}
