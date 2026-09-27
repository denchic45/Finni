package com.hackathon.finni.features.levels.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.hackathon.finni.core.ui.components.CoinBalance
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.btn_close
import org.jetbrains.compose.resources.painterResource

/**
 * Верхняя панель экрана уровней (LevelsTopBar):
 * - Глянцевая золотисто-янтарная плашка со скругленным низом (как на референсе)
 * - Кнопка возврата «Домой» слева с тактильной анимацией нажатия
 * - Баланс монет по центру/справа
 * - Кнопка-крестик («btn_close») в правом углу для закрытия экрана и возврата на главный
 */
@Composable
fun LevelsTopBar(
    coins: Int,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val barShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = barShape,
                ambientColor = Color(0x66B45700),
                spotColor = Color(0x66B45700)
            )
            .clip(barShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFB81E),
                        Color(0xFFFFA000),
                        Color(0xFFE87E00),
                        Color(0xFFD36800)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE58F),
                        Color(0xFFFFA000),
                        Color(0xFF984400)
                    )
                ),
                shape = barShape
            )
            .padding(top = statusBarPadding)
    ) {
        // Глянцевый световой блик в верхней части панели
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.0f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Баланс монет слева
            CoinBalance(
                coins = coins,
                height = 44.dp
            )

            // Крестик закрытия экрана справа
            CloseButton(onClick = onHomeClick)
        }
    }
}

@Composable
private fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "close_btn_scale"
    )

    Box(
        modifier = modifier
            .size(46.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.btn_close),
            contentDescription = "Закрыть",
            modifier = Modifier.size(46.dp),
            contentScale = ContentScale.Fit
        )
    }
}
