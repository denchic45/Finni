package com.hackathon.finni.features.levels.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.GameText
import com.hackathon.finni.core.theme.comfortaaFontFamily
import com.hackathon.finni.features.levels.model.LevelItem
import com.hackathon.finni.features.levels.model.LevelStatus
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_level_completed
import com.hackathon.finni.resources.ic_level_current
import com.hackathon.finni.resources.ic_level_locked
import com.hackathon.finni.resources.ic_level_pin_finni
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

/**
 * Интерактивный узел уровня на карте:
 * - COMPLETED: золотой диск с зеленой галочкой и 3 звездами
 * - CURRENT: светящаяся золотая 3D-кнопка с номером уровня и парящим пином Финни
 * - LOCKED: деревянный диск с замком и покачиванием при нажатии
 */
@Composable
fun LevelNodeItem(
    level: LevelItem,
    onClick: (LevelItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }

    // Анимация покачивания (shake) для заблокированных уровней
    val shakeOffset = remember { Animatable(0f) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 600f),
        label = "level_node_press"
    )

    Box(
        modifier = modifier
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .scale(pressScale)
            .pointerInput(level.id) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        if (level.status == LevelStatus.LOCKED) {
                            coroutineScope.launch {
                                // Эффект тряски при клике на замок
                                repeat(3) {
                                    shakeOffset.animateTo(-8f, tween(50))
                                    shakeOffset.animateTo(8f, tween(50))
                                }
                                shakeOffset.animateTo(0f, tween(50))
                            }
                        }
                        onClick(level)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        when (level.status) {
            LevelStatus.COMPLETED -> CompletedNode(level = level)
            LevelStatus.CURRENT -> CurrentNode(level = level)
            LevelStatus.LOCKED -> LockedNode(level = level)
        }
    }
}

@Composable
private fun CompletedNode(
    level: LevelItem,
    modifier: Modifier = Modifier
) {
    // В референсе узел пройденного уровня имеет размер ~84.dp с 3 звездами сверху
    Box(
        modifier = modifier.size(86.dp, 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_level_completed),
            contentDescription = "Уровень ${level.number}: пройден",
            modifier = Modifier.size(86.dp, 80.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun CurrentNode(
    level: LevelItem,
    modifier: Modifier = Modifier
) {
    // Бесконечная анимация парения пина Финни
    val infiniteTransition = rememberInfiniteTransition(label = "pin_bobbing")
    val pinFloatOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pin_offset_y"
    )

    // Пульсация сияния вокруг кнопки
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = modifier.size(96.dp, 140.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Парящий пин Финни с указателем
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (pinFloatOffsetY).dp)
                .size(68.dp, 80.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_level_pin_finni),
                contentDescription = "Финни здесь",
                modifier = Modifier.size(68.dp, 80.dp),
                contentScale = ContentScale.Fit
            )
        }

        // 2. Светящаяся золотая 3D-кнопка с номером уровня
        Box(
            modifier = Modifier
                .size(90.dp, 84.dp)
                .scale(glowScale),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_level_current),
                contentDescription = "Текущий уровень ${level.number}",
                modifier = Modifier.size(90.dp, 84.dp),
                contentScale = ContentScale.Fit
            )

            // Номер уровня в центре кнопки
            GameText(
                text = level.number.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                strokeColor = Color(0xFF4A2508),
                strokeWidth = 3f,
                shadowColor = Color(0x66000000),
                shadowOffset = Offset(0f, 2f),
                shadowRadius = 2f,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = comfortaaFontFamily,
                    lineHeight = 26.sp,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both
                    )
                )
            )
        }
    }
}

@Composable
private fun LockedNode(
    level: LevelItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(76.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_level_locked),
            contentDescription = "Уровень ${level.number}: заблокирован",
            modifier = Modifier.size(76.dp),
            contentScale = ContentScale.Fit
        )
    }
}
