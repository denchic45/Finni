package com.hackathon.finni.core.ui.components.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.GameText
import com.hackathon.finni.core.theme.GameTheme
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_game_coin
import org.jetbrains.compose.resources.painterResource

/**
 * Бейдж баланса монет из референса:
 * 3D-золотая монетка слева, накладывающаяся на кремовую капсулу с шоколадным контуром.
 */
@Composable
fun GameCoinBadge(
    coins: Int,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    onClick: (() -> Unit)? = null
) {
    val colors = GameTheme.colors
    val coinSize = height + 10.dp
    val pillShape = RoundedCornerShape(height / 2f)

    Box(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.CenterStart
    ) {
        // Фоновая кремовая капсула
        Box(
            modifier = Modifier
                .padding(start = coinSize * 0.42f)
                .height(height)
                .shadow(elevation = 3.dp, shape = pillShape)
                .clip(pillShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.surfaceCreamLight,
                            colors.surfaceCreamBase,
                            colors.surfaceCreamDark
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    color = colors.borderDark,
                    shape = pillShape
                )
                .padding(start = coinSize * 0.55f, end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Текст суммы с анимацией смены числа
            AnimatedContent(
                targetState = coins,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { height -> height } togetherWith
                                slideOutVertically { height -> -height }
                    } else {
                        slideInVertically { height -> -height } togetherWith
                                slideOutVertically { height -> height }
                    }
                },
                label = "coins_counter"
            ) { targetCoins ->
                GameText(
                    text = targetCoins.toString(),
                    color = Color(0xFF4A250E),
                    strokeColor = null,
                    shadowColor = null,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Выпуклая 3D-монетка слева
        Box(
            modifier = Modifier
                .size(coinSize)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_game_coin),
                contentDescription = "Монеты",
                tint = Color.Unspecified,
                modifier = Modifier.size(coinSize)
            )
        }
    }
}
