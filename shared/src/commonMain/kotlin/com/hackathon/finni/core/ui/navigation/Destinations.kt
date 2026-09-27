package com.hackathon.finni.core.ui.navigation

import com.hackathon.finni.core.ui.components.game.GameTab
import com.hackathon.finni.core.ui.navigation.router.Destination
import com.hackathon.finni.core.ui.navigation.router.Modal
import com.hackathon.finni.core.ui.navigation.router.TopLevelRoute
import kotlinx.serialization.Serializable

@Serializable
data object Splash : TopLevelRoute

@Serializable
data object Home : TopLevelRoute

@Serializable
data class Confirmation(val title: String, val text: String? = null) : Modal

@Serializable
data class OverlayImages(val urls: List<String>, val initialIndex: Int) : Modal

@Serializable
data object GameUiShowcase : Destination

// Экраны игровых разделов нижнего меню (GameTab)
@Serializable
data object TasksScreen : Destination

@Serializable
data object PiggyScreen : Destination

@Serializable
data object ShopScreen : Destination

@Serializable
data object LevelsScreen : Destination

val appTabs = listOf(
    Home
)

/**
 * Определяет соответствующую вкладку [GameTab] по текущему открытому экрану в стеке [Destination].
 * Если открыт главный экран комнаты (Home), заставка или иной экран — возвращает null (все кнопки неактивны).
 */
fun Destination?.toGameTab(): GameTab? {
    if (this == null) return null
    return when (this) {
        is TasksScreen -> GameTab.Tasks
        is PiggyScreen -> GameTab.Piggy
        is ShopScreen -> GameTab.Shop
        is LevelsScreen -> GameTab.Levels
        else -> {
            val name = this::class.simpleName?.lowercase() ?: ""
            when {
                name.contains("task") -> GameTab.Tasks
                name.contains("piggy") || name.contains("saving") -> GameTab.Piggy
                name.contains("shop") || name.contains("store") -> GameTab.Shop
                name.contains("level") -> GameTab.Levels
                else -> null
            }
        }
    }
}