package com.hackathon.finni.core.ui.components.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hackathon.finni.core.theme.GameTheme
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_game_bone_bowl
import org.jetbrains.compose.resources.painterResource

/**
 * Круговой сегментированный индикатор сытости с интерактивной миской по центру
 * (соответствует референсу в левом верхнем углу).
 *
 * @param hungerValue Значение сытости от 0 до 5
 * @param maxHunger Максимальное значение сытости (по умолчанию 5)
 * @param segmentsCount Количество сегментов шкалы (по умолчанию 5)
 * @param onBowlClick Клик по центральной кнопке-миске (покормить питомца)
 */
@Composable
fun HungerRadialGauge(
    hungerValue: Int,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    maxHunger: Int = 5,
    segmentsCount: Int = 5,
    onBowlClick: () -> Unit = {}
) {
    val animatedHunger by animateFloatAsState(
        targetValue = hungerValue.coerceIn(0, maxHunger).toFloat(),
        animationSpec = tween(durationMillis = 400),
        label = "hunger_progress"
    )

    val colors = GameTheme.colors

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Фоновое темное кольцо и сегментированные дуги
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = (size * 0.16f).toPx()
            val arcSize = size.toPx() - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcRectSize = Size(arcSize, arcSize)

            // Темный общий трек под шкалой
            drawCircle(
                color = Color(0xFF261408),
                radius = size.toPx() / 2f
            )

            // Внешний контур темного диска
            drawCircle(
                color = Color(0xFF4A250E),
                radius = size.toPx() / 2f,
                style = Stroke(width = 3.dp.toPx())
            )

            // Сегментированная дуга
            val totalAngle = 360f
            val gapAngle = 6f
            val segmentAngle = (totalAngle / segmentsCount) - gapAngle

            val filledSegmentsRatio = animatedHunger / maxHunger.toFloat()
            val activeSegmentsCount = (filledSegmentsRatio * segmentsCount)

            for (i in 0 until segmentsCount) {
                // Начинаем снизу-слева как на референсе
                val startAngle = 135f + i * (segmentAngle + gapAngle)
                val isFilled = i < activeSegmentsCount

                if (isFilled) {
                    // Яркий золотисто-оранжевый заполненный сегмент
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                colors.hungerArcFilledStart,
                                colors.hungerArcFilledEnd,
                                colors.hungerArcFilledStart
                            )
                        ),
                        startAngle = startAngle,
                        sweepAngle = segmentAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcRectSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Шоколадная обводка сегмента
                    drawArc(
                        color = Color(0xFF4E260F),
                        startAngle = startAngle,
                        sweepAngle = segmentAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcRectSize,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else {
                    // Пустой темный сегмент
                    drawArc(
                        color = Color(0xFF381D0D),
                        startAngle = startAngle,
                        sweepAngle = segmentAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcRectSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Тонкая рамка пустого сегмента
                    drawArc(
                        color = Color(0xFF5A2F16),
                        startAngle = startAngle,
                        sweepAngle = segmentAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcRectSize,
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Центральная интерактивная кнопка-миска
        val bowlButtonSize = size * 0.58f
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        Box(
            modifier = Modifier
                .size(bowlButtonSize)
                .offset(y = if (isPressed) 2.dp else 0.dp)
                .shadow(elevation = if (isPressed) 2.dp else 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFB74D),
                            Color(0xFFF57C00),
                            Color(0xFFE65100)
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    color = Color(0xFF52270D),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onBowlClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Верхний глянцевый блик на кнопке
            Box(
                modifier = Modifier
                    .size(bowlButtonSize)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = bowlButtonSize.value * 1.2f
                        )
                    )
            )

            // Иконка миски с косточкой
            Icon(
                painter = painterResource(Res.drawable.ic_game_bone_bowl),
                contentDescription = "Покормить питомца",
                tint = Color.Unspecified,
                modifier = Modifier.size(bowlButtonSize * 0.72f)
            )
        }
    }
}
