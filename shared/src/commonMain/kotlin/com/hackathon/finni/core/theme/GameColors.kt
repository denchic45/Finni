package com.hackathon.finni.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class GameColors(
    // --- Основные акценты (Янтарно-золотые) ---
    val primaryGradientStart: Color = Color(0xFFFFA726),
    val primaryGradientEnd: Color = Color(0xFFF57C00),
    val primaryBorder: Color = Color(0xFF5A2C10),
    val primaryBevel: Color = Color(0xFFC75B00),
    val primaryHighlight: Color = Color(0xFFFFE082),

    // --- Золотые монеты и награды ---
    val coinGoldLight: Color = Color(0xFFFFEE58),
    val coinGoldBase: Color = Color(0xFFFFB300),
    val coinGoldDark: Color = Color(0xFFF57F17),
    val coinBorder: Color = Color(0xFF5A2C10),

    // --- Шоколадно-древесные контуры и тени ---
    val borderDark: Color = Color(0xFF4A240C),
    val borderMedium: Color = Color(0xFF6B3614),
    val bevelBottom: Color = Color(0xFF381806),
    val shadowColor: Color = Color(0x662A1405),

    // --- Кремовые фоновые поверхности (Плашки, карточки, капсулы) ---
    val surfaceCreamLight: Color = Color(0xFFFFFDF7),
    val surfaceCreamBase: Color = Color(0xFFFFF3D6),
    val surfaceCreamDark: Color = Color(0xFFFFE4B0),
    val surfaceCreamBorder: Color = Color(0xFF703816),

    // --- Вторичные кнопки (Светлые/кремовые) ---
    val secondaryGradientStart: Color = Color(0xFFFFFDF5),
    val secondaryGradientEnd: Color = Color(0xFFFFE8BC),
    val secondaryBevel: Color = Color(0xFFDEC08E),

    // --- Зеленые кнопки (Успех/Подтверждение) ---
    val successGradientStart: Color = Color(0xFF66BB6A),
    val successGradientEnd: Color = Color(0xFF388E3C),
    val successBorder: Color = Color(0xFF1B5E20),
    val successBevel: Color = Color(0xFF2E7D32),

    // --- Красные кнопки (Опасность/Отмена) ---
    val dangerGradientStart: Color = Color(0xFFEF5350),
    val dangerGradientEnd: Color = Color(0xFFD32F2F),
    val dangerBorder: Color = Color(0xFF5C1010),
    val dangerBevel: Color = Color(0xFFB71C1C),

    // --- Фиолетовые акценты (Цвет дракончика Финни) ---
    val dragonPurpleLight: Color = Color(0xFFBA68C8),
    val dragonPurpleBase: Color = Color(0xFF9C27B0),
    val dragonPurpleDark: Color = Color(0xFF6A1B9A),

    // --- Индикаторы настроения (Mood) ---
    val moodHappy: Color = Color(0xFFFFB300),
    val moodNeutral: Color = Color(0xFFFFCA28),
    val moodSad: Color = Color(0xFF78909C),
    val moodFrame: Color = Color(0xFF5D2E0C),

    // --- Время суток и жетоны (TimeOfDay) ---
    val timeBadgeBackground: Color = Color(0xFF262C3A),
    val timeBadgeBorder: Color = Color(0xFF161922),
    val sunGold: Color = Color(0xFFFFD54F),
    val moonBlue: Color = Color(0xFF4FC3F7),

    // --- Навбар (Нижняя панель) ---
    val navBarStart: Color = Color(0xFFFFB74D),
    val navBarEnd: Color = Color(0xFFF57C00),
    val navBarBorder: Color = Color(0xFF5A2C10),
    val navBarItemActiveStart: Color = Color(0xFFFFE082),
    val navBarItemActiveEnd: Color = Color(0xFFFFA726),
    val navBarItemInactiveStart: Color = Color(0xFFFFA726),
    val navBarItemInactiveEnd: Color = Color(0xFFF57C00),

    // --- Шкала сытости (Радиальная дуга) ---
    val hungerArcFilledStart: Color = Color(0xFFFFEB3B),
    val hungerArcFilledEnd: Color = Color(0xFFFF9800),
    val hungerArcEmpty: Color = Color(0xFF381D0D),
    val hungerArcBorder: Color = Color(0xFF4E260F)
)

val LocalGameColors = staticCompositionLocalOf { GameColors() }

object GameTheme {
    val colors: GameColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGameColors.current

    val defaultBorderWidth: Dp = 2.5.dp
    val defaultBevelHeight: Dp = 4.dp
    val defaultPressOffset: Dp = 3.dp
    val defaultCornerRadius: Dp = 18.dp

    fun primaryButtonBrush(colors: GameColors = GameColors()): Brush = Brush.verticalGradient(
        listOf(colors.primaryGradientStart, colors.primaryGradientEnd)
    )

    fun secondaryButtonBrush(colors: GameColors = GameColors()): Brush = Brush.verticalGradient(
        listOf(colors.secondaryGradientStart, colors.secondaryGradientEnd)
    )

    fun successButtonBrush(colors: GameColors = GameColors()): Brush = Brush.verticalGradient(
        listOf(colors.successGradientStart, colors.successGradientEnd)
    )

    fun dangerButtonBrush(colors: GameColors = GameColors()): Brush = Brush.verticalGradient(
        listOf(colors.dangerGradientStart, colors.dangerGradientEnd)
    )

    fun creamSurfaceBrush(colors: GameColors = GameColors()): Brush = Brush.verticalGradient(
        listOf(colors.surfaceCreamLight, colors.surfaceCreamBase)
    )

    fun goldCoinBrush(colors: GameColors = GameColors()): Brush = Brush.verticalGradient(
        listOf(colors.coinGoldLight, colors.coinGoldBase, colors.coinGoldDark)
    )

    fun navBarBrush(colors: GameColors = GameColors()): Brush = Brush.verticalGradient(
        listOf(colors.navBarStart, colors.navBarEnd)
    )
}
