package com.hackathon.finni.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.GameText
import com.hackathon.finni.core.theme.comfortaaFontFamily
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.toGameTab
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_levels
import com.hackathon.finni.resources.ic_piggy
import com.hackathon.finni.resources.ic_shop
import com.hackathon.finni.resources.ic_tasks
import com.hackathon.finni.resources.menu_cell
import com.hackathon.finni.resources.menu_cell_active
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

typealias GameTab = com.hackathon.finni.core.ui.components.game.GameTab

/**
 * Игровая нижняя навигационная панель (GameBottomBar):
 * - Скругления только в верхних углах (нижние углы прямые, без скруглений)
 * - Белая выразительная обводка в мультяшном стиле
 * - Квадратные подложки кнопок ([menu_cell.png] / [menu_cell_active.png]),
 *   скругляемые по верхним углам контейнера
 * - 4 вкладки: Задачи, Копилка, Магазин, Уровни
 */
@Composable
fun GameBottomBar(
    modifier: Modifier = Modifier,
    selectedTab: GameTab? = null,
    onTabSelected: (GameTab) -> Unit = {},
    router: Router = koinInject(),
    height: Dp = 78.dp,
    shape: RoundedCornerShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
) {
    GameBottomBarInternal(
        modifier = modifier,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        router = router,
        height = height,
        shape = shape
    )
}

@Composable
fun GameBottomBar(
    router: Router?,
    modifier: Modifier = Modifier,
    selectedTab: GameTab? = null,
    onTabSelected: (GameTab) -> Unit = {},
    height: Dp = 78.dp,
    shape: RoundedCornerShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
) {
    GameBottomBarInternal(
        modifier = modifier,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        router = router,
        height = height,
        shape = shape
    )
}

@Composable
private fun GameBottomBarInternal(
    modifier: Modifier = Modifier,
    selectedTab: GameTab? = null,
    onTabSelected: (GameTab) -> Unit = {},
    router: Router? = null,
    height: Dp = 78.dp,
    shape: RoundedCornerShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
) {
    val navState by router?.state?.collectAsState() ?: remember { mutableStateOf(null) }
    val activeTabFromStack = remember(navState) {
        navState?.flattenedBackStack?.lastOrNull()?.toGameTab()
    }
    val effectiveTab = selectedTab ?: activeTabFromStack

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // 1. Мягкая внешняя тень нижней панели
                .shadow(
                    elevation = 8.dp,
                    shape = shape,
                    ambientColor = Color(0x40000000),
                    spotColor = Color(0x60000000)
                )
                // 2. Белая обводка как на игровых плашках референса
                .border(
                    width = 2.dp,
                    color = Color.White,
                    shape = shape
                )
                // 3. Скругление контейнера по верхним углам
                .clip(shape)
                .background(Color(0xFFFFA726))
                .height(height)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameTab.entries.forEach { tab ->
                    BottomBarItem(
                        title = tab.title,
                        icon = painterResource(tab.iconRes),
                        isSelected = tab == effectiveTab,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }

        // 2. Непрозрачный системный нижний статусбар/навигационная зона в цвет темы
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding()
        )
    }
}

/**
 * Элемент нижней навигационной панели (кнопка вкладки):
 * - Фоновая квадратная ячейка-подложка:
 *   [menu_cell_active.png] для активной вкладки,
 *   [menu_cell.png] для неактивных вкладок
 * - 3D-иконка по центру ([ic_tasks.png], [ic_piggy.png], [ic_shop.png], [ic_levels.png]), увеличенная на 4 dp (38.dp)
 * - Утолщенный жирный шрифт Comfortaa ([FontWeight.ExtraBold])
 * - Тактильный эффект смещения при нажатии
 */
@Composable
fun BottomBarItem(
    title: String,
    icon: Painter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = title
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val itemOffset by animateDpAsState(
        targetValue = if (isPressed) 2.dp else if (isSelected) (-1).dp else 0.dp,
        animationSpec = tween(100),
        label = "bottom_bar_item_offset"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Фоновая графическая ячейка-подложка
        Image(
            painter = painterResource(
                if (isSelected) Res.drawable.menu_cell_active else Res.drawable.menu_cell
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Контент кнопки: иконка и текст с Comfortaa
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = itemOffset)
                .padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка увеличена на 4 dp (с 34.dp до 38.dp)
            Image(
                painter = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(38.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            GameText(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFFFFF8EC),
                strokeColor = Color(0xFF4A240C),
                strokeWidth = if (isSelected) 4f else 3.5f,
                shadowColor = Color(0x66000000),
                shadowOffset = Offset(0f, 1.5f),
                shadowRadius = 2f,
                style = TextStyle(fontFamily = comfortaaFontFamily)
            )
        }
    }
}

/**
 * Перегрузка [BottomBarItem], принимающая ресурс [DrawableResource].
 */
@Composable
fun BottomBarItem(
    title: String,
    iconRes: DrawableResource,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = title
) {
    BottomBarItem(
        title = title,
        icon = painterResource(iconRes),
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier,
        contentDescription = contentDescription
    )
}

/**
 * Превью нижней панели со сменой выбранной вкладки для Compose Preview.
 */
@Preview
@Composable
fun GameBottomBarPreview() {
    var selectedTab by remember { mutableStateOf<GameTab?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8D7B8))
            .padding(top = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        GameBottomBar(
            router = null,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}
