package com.hackathon.finni.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.comfortaaFontFamily
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.btn_close
import com.hackathon.finni.resources.dialog_header_plate
import org.jetbrains.compose.resources.painterResource

/**
 * Универсальный игровой диалог [GameDialog] в точном соответствии с дизайн-системой Финни:
 *
 * 1. Полупрозрачный затемняющий фон (Scrim/Backdrop).
 * 2. Глубокий шоколадный фон контейнера (`Color(0xFF351C0F)`).
 * 3. Многослойная золотистая рамка с фаской и внутренним затемнением.
 * 4. Верхний декоративный баннер с заголовком ([Res.drawable.dialog_header_plate]).
 * 5. Фирменный круглый красный 3D-крестик закрытия ([Res.drawable.btn_close]) в правом верхнем углу.
 * 6. Плавная анимация появления (масштабирование + затухание).
 */
@Composable
fun GameDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    dismissOnBackdropClick: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x8A000000))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        if (dismissOnBackdropClick) {
                            onDismissRequest()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(
                    initialScale = 0.82f,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f)
                ) + fadeIn(animationSpec = tween(200)),
                exit = scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(150)
                ) + fadeOut(animationSpec = tween(150))
            ) {
                Box(
                    modifier = modifier
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { /* блокируем клики сквозь диалог */ }
                        ),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // --- ОСНОВНОЙ ТЕМНО-ШОКОЛАДНЫЙ КОНТЕЙНЕР С ЗОЛОТИСТОЙ ОБВОДКОЙ ---
                    Box(
                        modifier = Modifier
                            .padding(top = 18.dp) // отступ для выступающего баннера
                            .fillMaxWidth()
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = Color.Black,
                                spotColor = Color.Black
                            )
                            // 1. Внешняя золотистая окантовка (Bevel gradient)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFFDF88),
                                        Color(0xFFE5A642),
                                        Color(0xFFB3732A)
                                    )
                                )
                            )
                            .padding(3.dp)
                            // 2. Внутренний темный контур глубины
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color(0xFF241107))
                            .padding(2.5.dp)
                            // 3. Основная темная шоколадная заливка
                            .clip(RoundedCornerShape(23.dp))
                            .background(Color(0xFF351C0F))
                            .padding(top = 32.dp, bottom = 18.dp, start = 14.dp, end = 14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            content()
                        }
                    }

                    // --- ВЕРХНИЙ ДЕКОРАТИВНЫЙ БАННЕР С НАЗВАНИЕМ ГЛАВЫ ---
                    if (!title.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth(0.86f)
                                .height(46.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.dialog_header_plate),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )
                            Text(
                                text = title,
                                fontFamily = comfortaaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .offset(y = (-1).dp),
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color(0xFF4A1E06),
                                        offset = Offset(0f, 3f),
                                        blurRadius = 3f
                                    )
                                )
                            )
                        }
                    }

                    // --- КНОПКА ЗАКРЫТИЯ («КРЕСТИК») ---
                    val closeInteraction = remember { MutableInteractionSource() }
                    val isClosePressed by closeInteraction.collectIsPressedAsState()
                    val closeScale by animateFloatAsState(
                        targetValue = if (isClosePressed) 0.88f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
                        label = "close_button_scale"
                    )

                    Image(
                        painter = painterResource(Res.drawable.btn_close),
                        contentDescription = "Закрыть диалог",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = 6.dp)
                            .size(50.dp)
                            .scale(closeScale)
                            .clickable(
                                indication = null,
                                interactionSource = closeInteraction,
                                onClick = onDismissRequest
                            ),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
