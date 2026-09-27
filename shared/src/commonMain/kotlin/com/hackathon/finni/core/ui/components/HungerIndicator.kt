package com.hackathon.finni.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.hunger_0
import com.hackathon.finni.resources.hunger_1
import com.hackathon.finni.resources.hunger_2
import com.hackathon.finni.resources.hunger_3
import com.hackathon.finni.resources.hunger_4
import com.hackathon.finni.resources.hunger_5
import com.hackathon.finni.resources.hunger_6
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Возвращает соответствующий ресурс изображения для уровня сытости от 0 до 6:
 * - 0: все сегменты пустые (темный шоколад)
 * - 1..5: постепенное заполнение золотисто-янтарных сегментов
 * - 6: шкала заполнена на 100%
 */
fun hungerDrawableRes(level: Int): DrawableResource = when (level.coerceIn(0, 6)) {
    0 -> Res.drawable.hunger_0
    1 -> Res.drawable.hunger_1
    2 -> Res.drawable.hunger_2
    3 -> Res.drawable.hunger_3
    4 -> Res.drawable.hunger_4
    5 -> Res.drawable.hunger_5
    else -> Res.drawable.hunger_6
}

/**
 * Игровой компонент индикатора голода/сытости питомца (HungerIndicator):
 * - Отображает миску с косточкой, окруженную подковообразной 6-сегментной шкалой
 *   ([hunger_0.png] .. [hunger_6.png])
 * - Плавное обновление шкалы с помощью классической анимации [Crossfade]
 * - Идеальное попиксельное совпадение геометрии: миска и контур остаются стабильными,
 *   а светящиеся сегменты мягко заполняются или затухают
 * - Тактильный эффект пружинящего нажатия при клике
 *
 * @param hunger Текущий уровень сытости (от 0 до 6)
 * @param modifier Модификатор контейнера
 * @param size Размер индикатора (по умолчанию 72.dp)
 * @param animationDurationMillis Длительность анимации crossfade (по умолчанию 350 мс)
 * @param onClick Опциональный обработчик клика (например, покормить питомца)
 */
@Composable
fun HungerIndicator(
    hunger: Int,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    animationDurationMillis: Int = 300,
    onClick: (() -> Unit)? = null
) {
    val currentLevel = hunger.coerceIn(0, 6)

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
            targetState = currentLevel,
            animationDurationMillis = animationDurationMillis,
            modifier = Modifier.fillMaxSize()
        ) { targetLevel ->
            Image(
                painter = painterResource(hungerDrawableRes(targetLevel)),
                contentDescription = "Сытость: $targetLevel из 6",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Превью индикатора голода с интерактивным изменением уровня сытости.
 */
@Preview
@Composable
fun HungerIndicatorPreview() {
    var hungerLevel by remember { mutableStateOf(3) }

    Box(
        modifier = Modifier
            .background(Color(0xFFE8D7B8), RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Интерактивный индикатор (клик увеличивает сытость)
            HungerIndicator(
                hunger = hungerLevel,
                size = 90.dp,
                onClick = {
                    hungerLevel = if (hungerLevel >= 6) 0 else hungerLevel + 1
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ряд всех состояний индикатора от 0 до 6
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0..6).forEach { level ->
                    HungerIndicator(
                        hunger = level,
                        size = 38.dp
                    )
                }
            }
        }
    }
}
