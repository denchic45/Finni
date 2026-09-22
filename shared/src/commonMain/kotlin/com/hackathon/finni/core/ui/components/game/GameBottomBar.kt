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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.GameText
import com.hackathon.finni.core.theme.GameTheme
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_game_levels
import com.hackathon.finni.resources.ic_game_piggy
import com.hackathon.finni.resources.ic_game_shop
import com.hackathon.finni.resources.ic_game_tasks
import org.jetbrains.compose.resources.painterResource

enum class GameTab(val title: String) {
    Tasks("Задачи"),
    Piggy("Копилка"),
    Shop("Магазин"),
    Levels("Уровни")
}

/**
 * Игровая нижняя навигационная панель (GameBottomBar), соответствующая
 * предоставленному референсу:
 * - 4 мультяшные вкладки: Задачи, Копилка, Магазин, Уровни
 * - Теплый янтарно-оранжевый глянцевый стиль
 * - Выпуклая активная вкладка с 3D-эффектом
 */
@Composable
fun GameBottomBar(
    selectedTab: GameTab,
    onTabSelected: (GameTab) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 76.dp
) {
    val colors = GameTheme.colors
    val barShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Нижняя шоколадная тень бара
                drawRoundRect(
                    color = colors.borderDark,
                    topLeft = Offset(0f, 0f),
                    size = this.size,
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
            .clip(barShape)
            .background(GameTheme.navBarBrush(colors))
            .border(
                width = 3.dp,
                color = colors.navBarBorder,
                shape = barShape
            )
            .navigationBarsPadding()
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val painter = when (tab) {
                    GameTab.Tasks -> painterResource(Res.drawable.ic_game_tasks)
                    GameTab.Piggy -> painterResource(Res.drawable.ic_game_piggy)
                    GameTab.Shop -> painterResource(Res.drawable.ic_game_shop)
                    GameTab.Levels -> painterResource(Res.drawable.ic_game_levels)
                }

                GameBottomBarItem(
                    title = tab.title,
                    icon = painter,
                    isSelected = isSelected,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun GameBottomBarItem(
    title: String,
    icon: Painter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GameTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val itemShape = RoundedCornerShape(16.dp)

    val itemOffset by animateDpAsState(
        targetValue = if (isPressed) 2.dp else if (isSelected) (-2).dp else 0.dp,
        animationSpec = tween(100),
        label = "tab_offset"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .offset(y = itemOffset)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(elevation = 4.dp, shape = itemShape)
                        .clip(itemShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    colors.navBarItemActiveStart,
                                    colors.navBarItemActiveEnd
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0xFFFFF3D6),
                            shape = itemShape
                        )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка вкладки
            Icon(
                painter = icon,
                contentDescription = title,
                tint = Color.Unspecified,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Название вкладки с мультяшной обводкой
            GameText(
                text = title,
                fontSize = 13.sp,
                color = if (isSelected) Color.White else Color(0xFFFFF6E3),
                strokeColor = colors.borderDark,
                strokeWidth = if (isSelected) 4f else 3f
            )
        }
    }
}
