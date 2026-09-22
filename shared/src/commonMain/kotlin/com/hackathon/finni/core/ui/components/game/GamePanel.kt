package com.hackathon.finni.core.ui.components.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.GameText
import com.hackathon.finni.core.theme.GameTheme

/**
 * Мультяшная панель/карточка (GamePanel) с 3D-скосом, шоколадной рамкой
 * и теплой кремовой заливкой для диалогов, списков задач и магазинов.
 */
@Composable
fun GamePanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    cornerRadius: Dp = 20.dp,
    bevelHeight: Dp = 5.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = GameTheme.colors
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Опциональный заголовок-ленточка сверху
        if (title != null) {
            val titleShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .offset(y = 10.dp)
                    .shadow(elevation = 6.dp, shape = titleShape)
                    .clip(titleShape)
                    .background(GameTheme.primaryButtonBrush(colors))
                    .border(width = 2.dp, color = colors.borderDark, shape = titleShape)
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                GameText(
                    text = title,
                    fontSize = 17.sp,
                    color = Color.White,
                    strokeColor = colors.borderDark
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val cornerPx = cornerRadius.toPx()
                    val bevelPx = bevelHeight.toPx()
                    // Нижний 3D скос панели
                    drawRoundRect(
                        color = colors.bevelBottom,
                        topLeft = Offset(0f, bevelPx),
                        size = Size(this.size.width, this.size.height - bevelPx),
                        cornerRadius = CornerRadius(cornerPx, cornerPx)
                    )
                }
                .shadow(elevation = 4.dp, shape = shape)
                .clip(shape)
                .background(GameTheme.creamSurfaceBrush(colors))
                .border(
                    width = GameTheme.defaultBorderWidth,
                    color = colors.borderDark,
                    shape = shape
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}
