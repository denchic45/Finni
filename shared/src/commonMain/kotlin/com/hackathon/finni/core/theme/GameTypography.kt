package com.hackathon.finni.core.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun GameText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    strokeColor: Color? = GameTheme.colors.borderDark,
    strokeWidth: Float = 6f,
    shadowColor: Color? = Color(0x80000000),
    shadowOffset: Offset = Offset(0f, 3f),
    shadowRadius: Float = 4f,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current
) {
    val effectiveShadow = shadowColor?.let {
        Shadow(
            color = it,
            offset = shadowOffset,
            blurRadius = shadowRadius
        )
    }

    val baseStyle = style.copy(
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign ?: style.textAlign
    )

    if (strokeColor != null && strokeWidth > 0f) {
        Box(modifier = modifier) {
            // Контур текста
            Text(
                text = text,
                style = baseStyle.copy(
                    color = strokeColor,
                    drawStyle = Stroke(
                        width = strokeWidth,
                        join = StrokeJoin.Round
                    ),
                    shadow = effectiveShadow
                ),
                maxLines = maxLines,
                overflow = overflow
            )
            // Заливка текста
            Text(
                text = text,
                style = baseStyle.copy(
                    color = color
                ),
                maxLines = maxLines,
                overflow = overflow
            )
        }
    } else {
        Text(
            text = text,
            modifier = modifier,
            style = baseStyle.copy(
                color = color,
                shadow = effectiveShadow
            ),
            maxLines = maxLines,
            overflow = overflow
        )
    }
}
