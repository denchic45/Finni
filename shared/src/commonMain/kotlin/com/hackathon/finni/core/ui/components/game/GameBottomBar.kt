package com.hackathon.finni.core.ui.components.game

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hackathon.finni.core.ui.navigation.router.Destination
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.toGameTab
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_levels
import com.hackathon.finni.resources.ic_piggy
import com.hackathon.finni.resources.ic_shop
import com.hackathon.finni.resources.ic_tasks
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.koinInject
import com.hackathon.finni.core.ui.components.BottomBarItem as CoreBottomBarItem
import com.hackathon.finni.core.ui.components.GameBottomBar as CoreGameBottomBar

/**
 * Перечисление вкладок нижней панели игры Finni:
 * - [Tasks]: Задачи
 * - [Piggy]: Копилка
 * - [Shop]: Магазин
 * - [Levels]: Уровни
 */
enum class GameTab(
    val title: String,
    val iconRes: DrawableResource
) {
    Tasks("Задачи", Res.drawable.ic_tasks),
    Piggy("Копилка", Res.drawable.ic_piggy),
    Shop("Магазин", Res.drawable.ic_shop),
    Levels("Уровни", Res.drawable.ic_levels)
}

/**
 * Игровая нижняя навигационная панель (GameBottomBar):
 *
 * - По умолчанию все кнопки неактивны ([selectedTab] = null).
 * - Кнопка активна, если открыт соответствующий экран или диалог.
 * - Если [selectedTab] не задан явно, автоматически определяет активную кнопку
 *   по верхнему открытому экрану в текущем стеке навигации [Router].
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
    CoreGameBottomBar(
        router = router,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        modifier = modifier,
        height = height,
        shape = shape
    )
}

/**
 * Перегрузка [GameBottomBar] с опциональным роутером (подходит для Previews и тестирования).
 */
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
    CoreGameBottomBar(
        router = router,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        modifier = modifier,
        height = height,
        shape = shape
    )
}

/**
 * Перегрузка [GameBottomBar] с передачей явного списка экранов в стеке навигации [backStack].
 */
@Composable
fun GameBottomBar(
    backStack: List<Destination>,
    onTabSelected: (GameTab) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 78.dp,
    shape: RoundedCornerShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
) {
    val activeTab = remember(backStack) { backStack.lastOrNull()?.toGameTab() }

    CoreGameBottomBar(
        router = null,
        selectedTab = activeTab,
        onTabSelected = onTabSelected,
        modifier = modifier,
        height = height,
        shape = shape
    )
}

@Composable
fun BottomBarItem(
    title: String,
    icon: Painter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = title
) {
    CoreBottomBarItem(
        title = title,
        icon = icon,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier,
        contentDescription = contentDescription
    )
}
