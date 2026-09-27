package com.hackathon.finni.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Анимация смены изображений методом плавного наложения (Overlay Crossfade):
 * - Предыдущее состояние ([baseState]) остается на 100% непрозрачным на нижнем слое
 * - Новое состояние ([incomingState]) плавно проявляется поверх старого (alpha от 0f до 1f)
 * - Картинка ни в один момент времени не становится полупрозрачной и сквозь нее
 *   никогда не просвечивает фон комнаты (новая картинка просто закрывает старую)
 * - Отсутствует масштабирование
 *
 * @param targetState Текущее целевое состояние
 * @param modifier Модификатор контейнера
 * @param animationDurationMillis Длительность анимации проявления
 * @param content Composable для отрисовки состояния
 */
@Composable
fun <T> OverlayCrossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    animationDurationMillis: Int = 300,
    content: @Composable (T) -> Unit
) {
    var baseState by remember { mutableStateOf(targetState) }
    var incomingState by remember { mutableStateOf<T?>(null) }
    val alphaAnim = remember { Animatable(1f) }

    LaunchedEffect(targetState) {
        if (targetState != baseState) {
            incomingState = targetState
            alphaAnim.snapTo(0f)
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDurationMillis)
            )
            baseState = targetState
            incomingState = null
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Базовый нижний слой: старая картинка со 100% непрозрачностью (alpha = 1f)
        content(baseState)

        // 2. Верхний слой: новая картинка, плавно проявляющаяся поверх старой (alpha 0f -> 1f)
        incomingState?.let { next ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = alphaAnim.value }
            ) {
                content(next)
            }
        }
    }
}
