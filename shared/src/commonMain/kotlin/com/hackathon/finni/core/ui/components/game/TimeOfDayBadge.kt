package com.hackathon.finni.core.ui.components.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hackathon.finni.core.theme.GameTheme
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_game_moon
import com.hackathon.finni.resources.ic_game_sun
import org.jetbrains.compose.resources.painterResource

enum class GameTimePhase(val title: String) {
    Morning("Утро"),
    Day("День"),
    Evening("Вечер")
}

/**
 * Индикатор времени суток и солнечных жетонов (из референса в правом верхнем углу).
 *
 * @param phase Текущая фаза (Утро / День / Вечер)
 * @param tokensLeft Оставшиеся жетоны активности (0..3)
 * @param maxTokens Максимальное количество жетонов (обычно 3)
 */
@Composable
fun TimeOfDayBadge(
    phase: GameTimePhase,
    tokensLeft: Int = 3,
    maxTokens: Int = 3,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    onClick: (() -> Unit)? = null
) {
    val colors = GameTheme.colors
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF333A4D),
                        Color(0xFF222634),
                        Color(0xFF161922)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = Color(0xFF4C556D),
                shape = shape
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка фазы (Солнце или Луна)
            val iconRes =
                if (phase == GameTimePhase.Evening) Res.drawable.ic_game_moon else Res.drawable.ic_game_sun
            Icon(
                painter = painterResource(iconRes),
                contentDescription = phase.title,
                tint = Color.Unspecified,
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Точечные индикаторы солнечных жетонов (желтые кружочки)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until maxTokens) {
                    val isActive = i < tokensLeft
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) Color(0xFFFFD54F) else Color(0xFF555D75)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive) Color(0xFFFFA000) else Color(0xFF3A3F50),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}
