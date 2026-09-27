package com.hackathon.finni.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.GameText
import com.hackathon.finni.core.theme.comfortaaFontFamily
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.balance
import org.jetbrains.compose.resources.painterResource

/**
 * Игровой компонент баланса монет (CoinBalance):
 * - Графическая подложка [Res.drawable.balance] (золотая 3D-монета + карамельный слот в белой рамке)
 * - Счетчик денег внутри слота с анимацией изменения суммы
 * - Мультяшный шрифт Comfortaa ([comfortaaFontFamily]) с шоколадной обводкой и мягкой тенью
 *
 * @param coins Количество монет
 * @param modifier Модификатор контейнера
 * @param height Высота бейджа (по умолчанию 46.dp)
 * @param onClick Опциональный обработчик нажатия
 */
@Composable
fun CoinBalance(
    coins: Int,
    modifier: Modifier = Modifier,
    height: Dp = 46.dp,
    onClick: (() -> Unit)? = null
) {
    // Соотношение сторон плашки balance.png (938 x 480 ~ 1.954)
    val aspectRatio = 938f / 480f

    BoxWithConstraints(
        modifier = modifier
            .height(height)
            .aspectRatio(aspectRatio)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // 1. Графическая подложка бейджа (монета слева + карамельный слот)
        Image(
            painter = painterResource(Res.drawable.balance),
            contentDescription = "Баланс: $coins монет",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // 2. Слот счетчика монет:
        // В balance.png слот занимает область от 44% до 92% ширины плашки
        val slotStartPadding = maxWidth * 0.44f
        val slotEndPadding = maxWidth * 0.08f
        val dynamicFontSize = (maxHeight.value * 0.33f).sp

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = slotStartPadding, end = slotEndPadding),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = coins,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { h -> h } togetherWith
                                slideOutVertically { h -> -h }
                    } else {
                        slideInVertically { h -> -h } togetherWith
                                slideOutVertically { h -> h }
                    }
                },
                contentAlignment = Alignment.Center,
                label = "coin_balance_counter"
            ) { targetCoins ->
                GameText(
                    text = targetCoins.toString(),
                    fontSize = dynamicFontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    strokeColor = Color(0xFF3E1C08),
                    strokeWidth = 2.5f,
                    shadowColor = Color(0x66000000),
                    shadowOffset = Offset(0f, 1.2f),
                    shadowRadius = 1.5f,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = comfortaaFontFamily,
                        lineHeight = dynamicFontSize,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    )
                )
            }
        }
    }
}

/**
 * Превью компонента CoinBalance.
 */
@Preview
@Composable
fun CoinBalancePreview() {
    var coinCount by remember { mutableStateOf(150) }

    Box(
        modifier = Modifier
            .background(Color(0xFFE8D7B8), RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoinBalance(
                coins = coinCount,
                height = 48.dp,
                onClick = { coinCount += 25 }
            )

            Spacer(modifier = Modifier.width(16.dp))

            CoinBalance(
                coins = 1250,
                height = 42.dp
            )
        }
    }
}
