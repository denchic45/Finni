package com.hackathon.finni.core.ui.components.game

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.GameText
import com.hackathon.finni.core.theme.GameTheme

enum class GameButtonStyle {
    Primary,
    Secondary,
    Success,
    Danger,
    Purple,
    Wood
}

enum class GameButtonSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val fontSize: Int,
    val cornerRadius: Dp,
    val bevelHeight: Dp
) {
    Large(56.dp, 24.dp, 18, 22.dp, 5.dp),
    Medium(46.dp, 18.dp, 16, 18.dp, 4.dp),
    Small(36.dp, 12.dp, 14, 14.dp, 3.dp)
}

/**
 * Мультяшная 3D-кнопка (Jelly Button) с физическим эффектом нажатия,
 * верхним глянцевым бликом, нижним скосом и сочной шоколадной обводкой.
 */
@Composable
fun GameButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    style: GameButtonStyle = GameButtonStyle.Primary,
    size: GameButtonSize = GameButtonSize.Medium,
    enabled: Boolean = true,
    leadingIcon: Painter? = null,
    trailingIcon: Painter? = null,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val colors = GameTheme.colors

    val (brush, bevelColor, borderColor, textColor, strokeColor) = when (style) {
        GameButtonStyle.Primary -> ButtonPalette(
            brush = GameTheme.primaryButtonBrush(colors),
            bevel = colors.primaryBevel,
            border = colors.primaryBorder,
            text = Color.White,
            stroke = colors.borderDark
        )

        GameButtonStyle.Secondary -> ButtonPalette(
            brush = GameTheme.secondaryButtonBrush(colors),
            bevel = colors.secondaryBevel,
            border = colors.surfaceCreamBorder,
            text = colors.borderDark,
            stroke = null
        )

        GameButtonStyle.Success -> ButtonPalette(
            brush = GameTheme.successButtonBrush(colors),
            bevel = colors.successBevel,
            border = colors.successBorder,
            text = Color.White,
            stroke = colors.successBorder
        )

        GameButtonStyle.Danger -> ButtonPalette(
            brush = GameTheme.dangerButtonBrush(colors),
            bevel = colors.dangerBevel,
            border = colors.dangerBorder,
            text = Color.White,
            stroke = colors.dangerBorder
        )

        GameButtonStyle.Purple -> ButtonPalette(
            brush = Brush.verticalGradient(
                listOf(
                    colors.dragonPurpleLight,
                    colors.dragonPurpleDark
                )
            ),
            bevel = colors.dragonPurpleDark,
            border = Color(0xFF4A148C),
            text = Color.White,
            stroke = Color(0xFF38006B)
        )

        GameButtonStyle.Wood -> ButtonPalette(
            brush = Brush.verticalGradient(listOf(Color(0xFF8D5325), Color(0xFF5C2D0E))),
            bevel = Color(0xFF3B1A05),
            border = colors.borderDark,
            text = Color(0xFFFFE8B8),
            stroke = colors.borderDark
        )
    }

    val bevelHeight = size.bevelHeight
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) (bevelHeight - 1.dp) else 0.dp,
        animationSpec = tween(durationMillis = 60),
        label = "press_offset"
    )

    val shape = RoundedCornerShape(size.cornerRadius)

    Box(
        modifier = modifier
            .height(size.height + bevelHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            // Рисуем нижний объемный скос (bevel)
            .drawBehind {
                val cornerPx = size.cornerRadius.toPx()
                val bevelPx = bevelHeight.toPx()
                // Нижняя подложка (скос 3D)
                drawRoundRect(
                    color = bevelColor,
                    topLeft = Offset(0f, bevelPx),
                    size = Size(this.size.width, this.size.height - bevelPx),
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                )
                // Темная внешняя тень под кнопкой
                drawRoundRect(
                    color = borderColor,
                    topLeft = Offset(0f, bevelPx),
                    size = Size(this.size.width, this.size.height - bevelPx),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // Лицевая сторона кнопки, смещающаяся вниз при нажатии
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(size.height)
                .offset(y = pressOffset)
                .shadow(
                    elevation = if (isPressed) 1.dp else 3.dp,
                    shape = shape,
                    clip = false
                )
                .clip(shape)
                .background(brush)
                .border(
                    width = GameTheme.defaultBorderWidth,
                    color = borderColor,
                    shape = shape
                )
        ) {
            // Верхний глянцевый блик (полупрозрачная полоска)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(size.height * 0.45f)
                    .clip(
                        RoundedCornerShape(
                            topStart = size.cornerRadius,
                            topEnd = size.cornerRadius
                        )
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
            )

            // Контент кнопки (текст / иконки)
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = size.horizontalPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (content != null) {
                    content()
                } else {
                    if (leadingIcon != null) {
                        Icon(
                            painter = leadingIcon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size((size.fontSize + 4).dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (text != null) {
                        GameText(
                            text = text,
                            color = textColor,
                            strokeColor = strokeColor,
                            fontSize = size.fontSize.sp
                        )
                    }

                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = trailingIcon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size((size.fontSize + 4).dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Квадратная 3D-кнопка для иконок (как кнопка шестеренки настроек в референсе).
 */
@Composable
fun GameIconButton(
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    style: GameButtonStyle = GameButtonStyle.Primary,
    cornerRadius: Dp = 16.dp,
    bevelHeight: Dp = 4.dp,
    iconSize: Dp = 28.dp,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val colors = GameTheme.colors

    val (brush, bevelColor, borderColor) = when (style) {
        GameButtonStyle.Primary -> Triple(
            GameTheme.primaryButtonBrush(colors),
            colors.primaryBevel,
            colors.primaryBorder
        )

        GameButtonStyle.Secondary -> Triple(
            GameTheme.secondaryButtonBrush(colors),
            colors.secondaryBevel,
            colors.surfaceCreamBorder
        )

        GameButtonStyle.Success -> Triple(
            GameTheme.successButtonBrush(colors),
            colors.successBevel,
            colors.successBorder
        )

        GameButtonStyle.Danger -> Triple(
            GameTheme.dangerButtonBrush(colors),
            colors.dangerBevel,
            colors.dangerBorder
        )

        GameButtonStyle.Purple -> Triple(
            Brush.verticalGradient(listOf(colors.dragonPurpleLight, colors.dragonPurpleDark)),
            colors.dragonPurpleDark,
            Color(0xFF4A148C)
        )

        GameButtonStyle.Wood -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF8D5325), Color(0xFF5C2D0E))),
            Color(0xFF3B1A05),
            colors.borderDark
        )
    }

    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) (bevelHeight - 1.dp) else 0.dp,
        animationSpec = tween(durationMillis = 60),
        label = "icon_press_offset"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(width = size, height = size + bevelHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .drawBehind {
                val cornerPx = cornerRadius.toPx()
                val bevelPx = bevelHeight.toPx()
                drawRoundRect(
                    color = bevelColor,
                    topLeft = Offset(0f, bevelPx),
                    size = Size(this.size.width, this.size.height - bevelPx),
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                )
                drawRoundRect(
                    color = borderColor,
                    topLeft = Offset(0f, bevelPx),
                    size = Size(this.size.width, this.size.height - bevelPx),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .offset(y = pressOffset)
                .shadow(
                    elevation = if (isPressed) 1.dp else 3.dp,
                    shape = shape,
                    clip = false
                )
                .clip(shape)
                .background(brush)
                .border(
                    width = GameTheme.defaultBorderWidth,
                    color = borderColor,
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Верхний глянцевый блик
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(size * 0.45f)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.5f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
            )

            Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

private data class ButtonPalette(
    val brush: Brush,
    val bevel: Color,
    val border: Color,
    val text: Color,
    val stroke: Color?
)
