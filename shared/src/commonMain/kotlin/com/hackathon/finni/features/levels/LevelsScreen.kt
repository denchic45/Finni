package com.hackathon.finni.features.levels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.comfortaaFontFamily
import com.hackathon.finni.core.ui.components.GameDialog
import com.hackathon.finni.features.levels.components.LevelsTopBar
import com.hackathon.finni.features.levels.components.ZigzagRoadMap
import com.hackathon.finni.features.levels.model.LevelItem
import com.hackathon.finni.features.levels.model.LevelStatus
import com.hackathon.finni.features.levels.model.LevelsData
import kotlin.math.roundToInt

/**
 * Экран уровней (LevelsScreen):
 * - Список уровней на извилистой мощеной дороге в виде зигзага (как в референсе)
 * - Верхний глянцевый бар с кнопкой «Домой» и балансом монет
 * - Интерактивные узлы: пройденные со звездами, текущий с парящим пином Финни, заблокированные с замками
 * - Нижнее меню с активной вкладкой «Уровни»
 * - Авто-фокусировка на текущем уровне при открытии
 */
@Composable
fun LevelsScreen(
    levels: List<LevelItem> = LevelsData.defaultLevels,
    coins: Int = 1150,
    onHomeClick: () -> Unit = {},
    onLevelClick: (LevelItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Выбранный уровень для показа модального диалога
    var selectedLevelForModal by remember { mutableStateOf<LevelItem?>(null) }

    // Автоматическая прокрутка к текущему уровню при запуске
    val currentLevelIndex = remember(levels) {
        levels.indexOfFirst { it.status == LevelStatus.CURRENT }.takeIf { it >= 0 } ?: 0
    }

    LaunchedEffect(scrollState.maxValue, currentLevelIndex) {
        if (scrollState.maxValue > 0) {
            // Расчет позиции текущего уровня относительно низа карты
            val rowHeightPx = with(density) { 115.dp.toPx() }
            val paddingBottomPx = with(density) { 60.dp.toPx() }
            val yFromBottom = paddingBottomPx + (currentLevelIndex * rowHeightPx)
            val targetScroll =
                (scrollState.maxValue - yFromBottom + with(density) { 200.dp.toPx() })
                    .coerceIn(0f, scrollState.maxValue.toFloat())
            scrollState.animateScrollTo(targetScroll.roundToInt())
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F5EE))
    ) {
        // 1. Полотно карты с прокруткой
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ZigzagRoadMap(
                levels = levels,
                onLevelClick = { level ->
                    selectedLevelForModal = level
                    onLevelClick(level)
                },
                paddingBottom = 60.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2. Верхняя глянцевая панель (LevelsTopBar с крестиком закрытия и кнопкой Домой)
        LevelsTopBar(
            coins = coins,
            onHomeClick = onHomeClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 3. Диалог информации об уровне
        selectedLevelForModal?.let { level ->
            LevelInfoDialog(
                level = level,
                onDismiss = { selectedLevelForModal = null },
                onStart = {
                    selectedLevelForModal = null
                    // Запуск выполнения задания уровня
                }
            )
        }
    }
}

/**
 * Диалог информации об уровне в стилистике GameDialog
 */
@Composable
private fun LevelInfoDialog(
    level: LevelItem,
    onDismiss: () -> Unit,
    onStart: () -> Unit
) {
    GameDialog(
        title = "УРОВЕНЬ ${level.number}",
        visible = true,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (level.status) {
                LevelStatus.LOCKED -> {
                    Text(
                        text = "🔒 Этот уровень пока закрыт.\n\nПройди предыдущие задания,\nчтобы открыть его!",
                        fontFamily = comfortaaFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFEE7C8),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    com.hackathon.finni.core.ui.components.game.GameButton(
                        onClick = onDismiss,
                        text = "ПОНЯТНО",
                        style = com.hackathon.finni.core.ui.components.game.GameButtonStyle.Wood,
                        size = com.hackathon.finni.core.ui.components.game.GameButtonSize.Medium,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }

                LevelStatus.CURRENT -> {
                    Text(
                        text = "⭐ ${level.title}\n\n${level.description}\n\nНаграда: +${level.rewardCoins} монет 🪙",
                        fontFamily = comfortaaFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    com.hackathon.finni.core.ui.components.game.GameButton(
                        onClick = onStart,
                        text = "НАЧАТЬ ЗАДАНИЕ",
                        style = com.hackathon.finni.core.ui.components.game.GameButtonStyle.Success,
                        size = com.hackathon.finni.core.ui.components.game.GameButtonSize.Large,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }

                LevelStatus.COMPLETED -> {
                    Text(
                        text = "✨ ${level.title}\n\nУровень успешно пройден!\nПолучено: ⭐⭐⭐ (3 звезды)",
                        fontFamily = comfortaaFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4FFC2),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    com.hackathon.finni.core.ui.components.game.GameButton(
                        onClick = onStart,
                        text = "ИГРАТЬ СНОВА",
                        style = com.hackathon.finni.core.ui.components.game.GameButtonStyle.Primary,
                        size = com.hackathon.finni.core.ui.components.game.GameButtonSize.Medium,
                        modifier = Modifier.fillMaxWidth(0.75f)
                    )
                }
            }
        }
    }
}
