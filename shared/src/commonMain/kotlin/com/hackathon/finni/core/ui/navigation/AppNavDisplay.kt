package com.hackathon.finni.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.hackathon.finni.core.ui.navigation.router.Destination
import com.hackathon.finni.core.ui.navigation.router.Modal

private class RetainedOverlay(
    val key: Modal,
    val controller: ExitController = ExitController()
)

@Composable
fun AppNavDisplay(
    backstack: List<Destination>,
    entryProvider: (Destination) -> NavEntry<Destination>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    sceneStrategies: List<SceneStrategy<Destination>> = emptyList(),
    transitionSpec: AnimatedContentTransitionScope<Scene<Destination>>.() -> ContentTransform = {
        ContentTransform(
            targetContentEnter = fadeIn(),
            initialContentExit = fadeOut()
        )
    },
    popTransitionSpec: AnimatedContentTransitionScope<Scene<Destination>>.() -> ContentTransform = transitionSpec,
    entryDecorators: List<NavEntryDecorator<Destination>> = emptyList()
) {
    // Находим индекс последнего полноэкранного маршрута
    val lastNonModalIndex = remember(backstack) {
        backstack.indexOfLast { it !is Modal }
    }

    // 1. Формируем базовый стек для основного NavDisplay (без оверлеев)
    val baseBackstack = remember(backstack) {
        if (lastNonModalIndex >= 0) {
            backstack.subList(0, lastNonModalIndex + 1)
        } else {
            listOf(backstack.first()) // Защита от пустого стека
        }
    }

    // Активные оверлеи: только те, что лежат ВЫШЕ последнего экрана
    val activeOverlayKeys = remember(backstack, lastNonModalIndex) {
        if (lastNonModalIndex >= 0) {
            backstack.drop(lastNonModalIndex + 1).filterIsInstance<Modal>()
        } else {
            backstack.filterIsInstance<Modal>()
        }
    }

    // Буфер для анимирования выхода оверлеев
    var displayedOverlays by remember {
        mutableStateOf<List<RetainedOverlay>>(emptyList())
    }

    // Синхронизация с activeOverlayKeys
    LaunchedEffect(activeOverlayKeys) {
        val currentKeysSet = activeOverlayKeys.toSet()

        // Помечаем шторки, покинувшие активный слой (удаленные или перекрытые новым экраном)
        displayedOverlays.forEach { overlay ->
            if (overlay.key !in currentKeysSet && !overlay.controller.isExiting) {
                overlay.controller.onFinished = {
                    displayedOverlays = displayedOverlays.filter { it.key != overlay.key }
                }
                overlay.controller.isExiting = true

                if (!overlay.controller.hasHandler) {
                    overlay.controller.finishExit()
                }
            }
        }

        // Добавляем новые оверлеи
        val newKeys = activeOverlayKeys.filter { key ->
            displayedOverlays.none { it.key == key }
        }
        if (newKeys.isNotEmpty()) {
            displayedOverlays = displayedOverlays + newKeys.map { RetainedOverlay(it) }
        }
    }

    Box(modifier = modifier) {
        NavDisplay(
            backStack = baseBackstack,
            onBack = onBack,
            entryProvider = entryProvider,
            sceneStrategies = sceneStrategies,
            transitionSpec = transitionSpec,
            popTransitionSpec = popTransitionSpec,
            entryDecorators = entryDecorators
        )

        // Слой оверлеев (Bottom Sheets / Dialogs)
        displayedOverlays.forEach { overlay ->
            key(overlay.key) {
                CompositionLocalProvider(LocalExitController provides overlay.controller) {
                    entryProvider(overlay.key).Content()
                }
            }
        }
    }
}
