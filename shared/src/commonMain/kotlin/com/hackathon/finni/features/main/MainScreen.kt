package com.hackathon.finni.features.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.comfortaaFontFamily
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
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

/**
 * Главный игровой экран («Дом Финни» / Main Screen), точно воссоздающий
 * расположение элементов из дизайн-референса:
 *
 * 1. Верхний левый угол:
 *    - [HungerIndicator] (миска с 6 сегментами сытости)
 *    - [MoodIndicator] (медальон эмодзи настроения)
 *    - [CoinBalance] (счетчик монет 1150)
 * 2. Верхний правый угол:
 *    - Кнопка настроек ([Res.drawable.btn_settings])
 *    - [TimeOfDayIndicator] (полупрозрачная плашка с солнцем/фазой суток, пристыкованная к правому краю)
 * 3. Центр экрана:
 *    - Интерактивная анимация касания и речевые реплики питомца
 * 4. Нижняя панель:
 *    - [GameBottomBar] (Задачи, Копилка, Магазин, Уровни)
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    initialCoins: Int = 1150,
    initialHunger: Int = 1,
    initialMood: PetMood = PetMood.Happy,
    initialTimePhase: GameTimePhase = GameTimePhase.Day,
    onSettingsClick: () -> Unit = {},
    onTabSelected: (GameTab) -> Unit = {}
) {
    var coins by remember { mutableStateOf(initialCoins) }
    var hunger by remember { mutableStateOf(initialHunger) }
    var mood by remember { mutableStateOf(initialMood) }
    var timePhase by remember { mutableStateOf(initialTimePhase) }
    var selectedTab by remember { mutableStateOf<GameTab?>(null) }
    var isTasksDialogOpen by remember { mutableStateOf(false) }

    // Интерактивная реакция питомца
    var petSpeechText by remember { mutableStateOf<String?>(null) }
    var petBounceTrigger by remember { mutableStateOf(0) }

    val petScale by animateFloatAsState(
        targetValue = if (petBounceTrigger % 2 == 1) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "pet_bounce_scale"
    )

    // Автоматическое скрытие реплики через 4 секунды
    LaunchedEffect(petSpeechText) {
        if (petSpeechText != null) {
            delay(4000)
            petSpeechText = null
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // --- 1. 3D СЦЕНА ФОНА (SceneView) ---
        SceneBackground(
            modifier = Modifier.fillMaxSize()
        )

        // --- 2. ЦЕНТРАЛЬНАЯ ИНТЕРАКТИВНАЯ ЗОНА ПИТОМЦА ---
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 240.dp, height = 280.dp)
                .offset(y = 20.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        petBounceTrigger++
                        petSpeechText = when {
                            hunger == 0 -> "В животе урчит... Пора покормить меня! 🍲"
                            mood == PetMood.Sad -> "Мне немного грустно... Давай позанимаемся! ✨"
                            timePhase == GameTimePhase.Night -> "Сладко зеваю... Спокойной ночи! 🌙"
                            timePhase == GameTimePhase.Morning -> "Доброе утро! Какой план на сегодня? ☀️"
                            coins >= 1000 -> "Ого, сколько монет! Скоро накопим на мечту! 🎯"
                            else -> "Привет, друг! Давай выполнять задания! 💜"
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Речевой пузырь над питомцем
            AnimatedVisibility(
                visible = petSpeechText != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-60).dp)
            ) {
                petSpeechText?.let { speech ->
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(2.dp, Color(0xFF5A3010), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = speech,
                            fontFamily = comfortaaFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF332010)
                        )
                    }
                }
            }
        }

        // --- 3. ВЕРХНИЙ СТАТУС-БАР (HUD) КАК В РЕФЕРЕНСЕ ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp),
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
                        onClick = {
                            hunger = (hunger + 1) % 7
                        }
                    )

                    // Медальон настроения (масштаб x1.5: 55 * 1.5 = 82.5 -> 83.dp)
                    MoodIndicator(
                        mood = mood,
                        size = 83.dp,
                        modifier = Modifier.padding(top = 2.dp),
                        onClick = {
                            mood = when (mood) {
                                PetMood.Happy -> PetMood.Neutral
                                PetMood.Neutral -> PetMood.Sad
                                PetMood.Sad -> PetMood.Happy
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Бейдж баланса монет (масштаб x1.5: 48 * 1.5 = 72.dp)
                CoinBalance(
                    coins = coins,
                    height = 72.dp,
                    onClick = {
                        coins += 25
                    }
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
                            onClick = onSettingsClick
                        ),
                    contentScale = ContentScale.Fit
                )

                // Индикатор времени суток (масштаб x1.2: 42 * 1.2 = 50.dp)
                TimeOfDayIndicator(
                    phase = timePhase,
                    height = 50.dp,
                    isDocked = true,
                    onClick = {
                        timePhase = when (timePhase) {
                            GameTimePhase.Morning -> GameTimePhase.Day
                            GameTimePhase.Day -> GameTimePhase.Evening
                            GameTimePhase.Evening -> GameTimePhase.Night
                            GameTimePhase.Night -> GameTimePhase.Morning
                        }
                    }
                )
            }
        }

        // --- 4. НИЖНЕЕ НАВИГАЦИОННОЕ МЕНЮ (GameBottomBar) ---
        GameBottomBar(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                selectedTab = tab
                if (tab == GameTab.Tasks) {
                    isTasksDialogOpen = true
                }
                onTabSelected(tab)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // --- 5. ДИАЛОГ СЮЖЕТНЫХ ЗАДАНИЙ ГЛАВЫ (TasksDialog) ---
        TasksDialog(
            visible = isTasksDialogOpen,
            onDismissRequest = {
                isTasksDialogOpen = false
                selectedTab = null
            },
            onTaskClick = { task ->
                // Нажатие на карточку задания
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
    MainScreen()
}
