package com.hackathon.finni.features.main

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hackathon.finni.core.ui.components.CoinBalance
import com.hackathon.finni.core.ui.components.GameBottomBar
import com.hackathon.finni.core.ui.components.HungerIndicator
import com.hackathon.finni.core.ui.components.MoodIndicator
import com.hackathon.finni.core.ui.components.TasksDialog
import com.hackathon.finni.core.ui.components.TimeOfDayIndicator
import com.hackathon.finni.core.ui.components.game.GameTab
import com.hackathon.finni.core.ui.components.game.GameTimePhase
import com.hackathon.finni.core.ui.components.game.PetMood
import com.hackathon.finni.features.main.components.SceneBackground
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.btn_settings
import com.hackathon.finni.resources.task_badge_star
import com.hackathon.finni.core.ui.components.GameTaskItem
import com.hackathon.finni.features.tasks.DilemmaCatalog
import com.hackathon.finni.core.ui.extension.getStringResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Главный игровой экран («Дом Финни» / Main Screen), подключенный к [MainScreenViewModel]:
 *
 * 1. Верхний левый угол:
 *    - [HungerIndicator] (миска с 6 сегментами сытости)
 *    - [MoodIndicator] (медальон эмодзи настроения)
 *    - [CoinBalance] (счетчик монет из Room)
 * 2. Верхний правый угол:
 *    - Кнопка настроек ([Res.drawable.btn_settings])
 *    - [TimeOfDayIndicator] (полупрозрачная плашка с солнцем/фазой суток, пристыкованная к правому краю)
 * 3. 3D сцена SceneView:
 *    - Свободный поворот камеры жестами по всему экрану, включая касания и свайпы по питомцу
 * 4. Нижняя панель:
 *    - [GameBottomBar] (Задачи, Копилка, Магазин, Уровни)
 * 5. Синхронное появление:
 *    - До загрузки 3D сцены интерфейс скрыт и появляется с синхронной плавной анимацией
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = koinViewModel(),
    onSettingsClick: () -> Unit = {},
    onTabSelected: (GameTab) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSceneReady by remember { mutableStateOf(false) }

    MainScreenContent(
        modifier = modifier,
        coins = uiState.coins,
        hunger = uiState.hunger,
        mood = uiState.mood,
        timePhase = uiState.timePhase,
        selectedTab = uiState.selectedTab,
        isTetrisUnlocked = uiState.isTetrisUnlocked,
        isTasksDialogOpen = uiState.isTasksDialogOpen,
        isSceneReady = isSceneReady,
        onSceneReady = { isSceneReady = true },
        onBowlClick = viewModel::onBowlClick,
        onMoodClick = viewModel::onMoodClick,
        onTimePhaseClick = viewModel::onAdvanceTimePhase,
        onSettingsClick = onSettingsClick,
        onTabSelected = { tab ->
            viewModel.onTabSelected(tab)
            onTabSelected(tab)
        },
        onDismissTasksDialog = viewModel::onDismissTasksDialog,
        onTaskClick = viewModel::onTaskClick,
        onTetrisClick = viewModel::onTetrisClick
    )
}

@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    coins: Int = 1150,
    hunger: Int = 4,
    mood: PetMood = PetMood.Happy,
    timePhase: GameTimePhase = GameTimePhase.Day,
    selectedTab: GameTab? = null,
    isTetrisUnlocked: Boolean = false,
    isTasksDialogOpen: Boolean = false,
    isSceneReady: Boolean = true,
    onSceneReady: () -> Unit = {},
    onBowlClick: () -> Unit = {},
    onMoodClick: () -> Unit = {},
    onTimePhaseClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onTabSelected: (GameTab) -> Unit = {},
    onDismissTasksDialog: () -> Unit = {},
    onTaskClick: (String) -> Unit = {},
    onTetrisClick: () -> Unit = {}
) {
    // Синхронная плавная анимация появления интерфейса вместе с 3D сценой
    val uiAlpha by animateFloatAsState(
        targetValue = if (isSceneReady) 1.0f else 0.0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = LinearOutSlowInEasing
        ),
        label = "ui_fade_in"
    )

    val isInteractive = uiAlpha > 0.5f

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // --- 1. 3D СЦЕНА ФОНА (SceneView) С ПОВОРОТОМ КАМЕРЫ ПО ВСЕМУ ЭКРАНУ ---
        SceneBackground(
            modifier = Modifier.fillMaxSize(),
            onSceneReady = onSceneReady
        )

        // --- 2. ВЕРХНИЙ СТАТУС-БАР (HUD) КАК В РЕФЕРЕНСЕ ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp)
                .graphicsLayer { alpha = uiAlpha },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // ЛЕВЫЙ БЛОК: Миска сытости + Эмодзи настроения + Баланс монет
            Column(
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Индикатор сытости (масштаб x1.5: 103 * 1.5 = 154.5 -> 155.dp)
                    HungerIndicator(
                        hunger = hunger,
                        size = 155.dp,
                        onClick = if (isInteractive) onBowlClick else ({})
                    )

                    // Медальон настроения (масштаб x1.5: 55 * 1.5 = 82.5 -> 83.dp)
                    MoodIndicator(
                        mood = mood,
                        size = 83.dp,
                        modifier = Modifier.padding(top = 2.dp),
                        onClick = if (isInteractive) onMoodClick else ({})
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Бейдж баланса монет (масштаб x1.5: 48 * 1.5 = 72.dp)
                CoinBalance(
                    coins = coins,
                    height = 72.dp,
                    onClick = {}
                )
            }

            // ПРАВЫЙ БЛОК: Кнопка настроек + Индикатор времени суток
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Кнопка настроек (оранжевый глянец с шестеренкой, масштаб x1.2: 46 * 1.2 = 55.dp)
                val settingsInteraction = remember { MutableInteractionSource() }
                val isSettingsPressed by settingsInteraction.collectIsPressedAsState()
                val settingsScale by animateFloatAsState(
                    targetValue = if (isSettingsPressed) 0.92f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                    label = "settings_scale"
                )

                Image(
                    painter = painterResource(Res.drawable.btn_settings),
                    contentDescription = "Настройки",
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .scale(settingsScale)
                        .size(55.dp)
                        .clickable(
                            indication = null,
                            interactionSource = settingsInteraction,
                            enabled = isInteractive,
                            onClick = onSettingsClick
                        ),
                    contentScale = ContentScale.Fit
                )

                // Индикатор времени суток (масштаб x1.2: 42 * 1.2 = 50.dp)
                TimeOfDayIndicator(
                    phase = timePhase,
                    height = 50.dp,
                    isDocked = true,
                    onClick = if (isInteractive) onTimePhaseClick else ({})
                )
            }
        }

        if (isTetrisUnlocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 148.dp, end = 14.dp)
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF6A48B9))
                    .border(3.dp, Color(0xFFE7D9FF), RoundedCornerShape(20.dp))
                    .clickable(onClick = onTetrisClick),
                contentAlignment = Alignment.Center
            ) {
                Text("▦\nТЕТРИС", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
            }
        }

        // --- 3. НИЖНЕЕ НАВИГАЦИОННОЕ МЕНЮ (GameBottomBar) ---
        GameBottomBar(
            selectedTab = selectedTab,
            onTabSelected = if (isInteractive) onTabSelected else ({}),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer { alpha = uiAlpha }
        )

        // --- 4. ДИАЛОГ СЮЖЕТНЫХ ЗАДАНИЙ ГЛАВЫ (TasksDialog) ---
        TasksDialog(
            visible = isTasksDialogOpen,
            onDismissRequest = onDismissTasksDialog,
            tasks = DilemmaCatalog.all.mapIndexed { index, task ->
                GameTaskItem(
                    id = task.id,
                    number = index + 1,
                    title = task.title.getStringResource(),
                    description = task.story.getStringResource(),
                    badgeRes = Res.drawable.task_badge_star
                )
            },
            onTaskClick = { task ->
                onTaskClick(task.id)
            }
        )
    }
}

/**
 * Превью главного экрана игры Финни.
 */
@Preview
@Composable
fun MainScreenPreview() {
    MainScreenContent()
}
