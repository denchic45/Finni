package com.hackathon.finni.features.game_ui_showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.finni.core.theme.GameText
import com.hackathon.finni.core.ui.components.game.GameBottomBar
import com.hackathon.finni.core.ui.components.game.GameButton
import com.hackathon.finni.core.ui.components.game.GameButtonSize
import com.hackathon.finni.core.ui.components.game.GameButtonStyle
import com.hackathon.finni.core.ui.components.game.GameCoinBadge
import com.hackathon.finni.core.ui.components.game.GameIconButton
import com.hackathon.finni.core.ui.components.game.GamePanel
import com.hackathon.finni.core.ui.components.game.GameTab
import com.hackathon.finni.core.ui.components.game.GameTimePhase
import com.hackathon.finni.core.ui.components.game.HungerRadialGauge
import com.hackathon.finni.core.ui.components.game.MoodIndicatorBadge
import com.hackathon.finni.core.ui.components.game.PetMood
import com.hackathon.finni.core.ui.components.game.TimeOfDayBadge
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_game_gear
import org.jetbrains.compose.resources.painterResource

/**
 * Интерактивная витрина игрового интерфейса Finni, воссоздающая
 * расположение элементов из предоставленного референса:
 * - Верхний левый блок: Датчик сытости с миской, Медальон настроения, Бейдж монет
 * - Верхний правый блок: Кнопка настроек, Индикатор времени суток и жетонов
 * - Центральная зона: Интерактивное управление состоянием и витрина кнопок/панелей
 * - Нижняя зона: Игровой 4-вкладочный навбар
 */
@Composable
fun GameUiShowcaseScreen(
    onBack: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var hunger by remember { mutableStateOf(75) }
    var coins by remember { mutableStateOf(1150) }
    var mood by remember { mutableStateOf(PetMood.Happy) }
    var timePhase by remember { mutableStateOf(GameTimePhase.Day) }
    var tokensLeft by remember { mutableStateOf(2) }
    var selectedTab by remember { mutableStateOf(GameTab.Tasks) }

    val cozyRoomBg = Brush.verticalGradient(
        listOf(
            Color(0xFF81D4FA), // Нежно-голубой цвет стены комнаты как на фото
            Color(0xFFB3E5FC),
            Color(0xFFFFF8E7), // Теплый тон пола
            Color(0xFFFFE0B2)
        )
    )

    Scaffold(
        bottomBar = {
            GameBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cozyRoomBg)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // --- ВЕРХНЯЯ СТАТУСНАЯ ПАНЕЛЬ ИЗ РЕФЕРЕНСА ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // ЛЕВЫЙ БЛОК: Сытость + Настроение, ниже Монеты
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Индикатор сытости с миской
                            HungerRadialGauge(
                                hungerValue = hunger,
                                size = 96.dp,
                                onBowlClick = {
                                    // Клик по миске кормит питомца
                                    hunger = (hunger + 20).coerceAtMost(100)
                                }
                            )

                            // Медальон настроения
                            MoodIndicatorBadge(
                                mood = mood,
                                size = 52.dp,
                                onClick = {
                                    mood = when (mood) {
                                        PetMood.Happy -> PetMood.Neutral
                                        PetMood.Neutral -> PetMood.Sad
                                        PetMood.Sad -> PetMood.Happy
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Бейдж баланса монет (1150 из референса)
                        GameCoinBadge(
                            coins = coins,
                            onClick = { coins += 50 }
                        )
                    }

                    // ПРАВЫЙ БЛОК: Шестеренка настроек + Солнце времени суток
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Кнопка возврата и настроек
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GameButton(
                                text = "◀ Меню",
                                onClick = onBack,
                                size = GameButtonSize.Small,
                                style = GameButtonStyle.Secondary
                            )
                            GameIconButton(
                                painter = painterResource(Res.drawable.ic_game_gear),
                                onClick = onSettingsClick,
                                size = 52.dp
                            )
                        }

                        // Индикатор времени суток и 3 жетонов
                        TimeOfDayBadge(
                            phase = timePhase,
                            tokensLeft = tokensLeft,
                            onClick = {
                                timePhase =
                                    if (timePhase == GameTimePhase.Day) GameTimePhase.Evening else GameTimePhase.Day
                                tokensLeft = if (tokensLeft > 0) tokensLeft - 1 else 3
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- ЦЕНТРАЛЬНАЯ ПАНЕЛЬ: ИНТЕРАКТИВНОЕ ТЕСТИРОВАНИЕ ---
                GamePanel(
                    title = "Управление состоянием"
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GameText(
                            text = "Нажмите на элементы верхнего бара или кнопки ниже:",
                            color = Color(0xFF5D2E0C),
                            strokeColor = null,
                            shadowColor = null,
                            fontSize = 14.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GameButton(
                                text = "Покормить (+20)",
                                onClick = { hunger = (hunger + 20).coerceAtMost(100) },
                                style = GameButtonStyle.Primary,
                                size = GameButtonSize.Small,
                                modifier = Modifier.weight(1f)
                            )
                            GameButton(
                                text = "Голод (-20)",
                                onClick = { hunger = (hunger - 20).coerceAtLeast(0) },
                                style = GameButtonStyle.Secondary,
                                size = GameButtonSize.Small,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GameButton(
                                text = "+100 монет",
                                onClick = { coins += 100 },
                                style = GameButtonStyle.Success,
                                size = GameButtonSize.Small,
                                modifier = Modifier.weight(1f)
                            )
                            GameButton(
                                text = "-50 монет",
                                onClick = { coins = (coins - 50).coerceAtLeast(0) },
                                style = GameButtonStyle.Danger,
                                size = GameButtonSize.Small,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GameButton(
                                text = "Дракончик Финни",
                                onClick = { },
                                style = GameButtonStyle.Purple,
                                size = GameButtonSize.Small,
                                modifier = Modifier.weight(1f)
                            )
                            GameButton(
                                text = "Древесная",
                                onClick = { },
                                style = GameButtonStyle.Wood,
                                size = GameButtonSize.Small,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- ИНФОРМАЦИЯ О ТЕКУЩЕЙ ВКЛАДКЕ ---
                GamePanel(
                    title = "Выбрано: ${selectedTab.title}"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        GameText(
                            text = when (selectedTab) {
                                GameTab.Tasks -> "Список финансовых заданий и поручений (чеклист)."
                                GameTab.Piggy -> "Копилка мечты с процентами и прогрессом накоплений."
                                GameTab.Shop -> "Магазин еды и эмоциональных покупок для дракончика."
                                GameTab.Levels -> "Карта 6 глав и приключений по финансовой грамотности."
                            },
                            color = Color(0xFF5D2E0C),
                            strokeColor = null,
                            shadowColor = null,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
