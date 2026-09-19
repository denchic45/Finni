package com.hackathon.finni.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.hackathon.finni.core.presentation.MainViewModel
import com.hackathon.finni.core.ui.navigation.router.Destination
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavigationContainer(
    viewModel: MainViewModel = koinViewModel()
) {
    val navigationState by viewModel.navigationState.collectAsState()
//    val currentTab = navigationState.currentTab
//    val currentDestination = navigationState.flattenedBackStack.last()

//    val showBottomBar = remember(currentDestination) {
//        when (currentDestination) {
//            is NavDestination -> true
//            is Modal -> navigationState.flattenedBackStack.last { it !is Modal } is NavDestination
//            else -> false
//        }
//    }

    AppNavDisplay(
        backStack = navigationState.flattenedBackStack,
        onBack = viewModel::onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AppNavDisplay(
    backStack: List<Destination>,
    onBack: () -> Unit
) {
    val myTransitionSpec: AnimatedContentTransitionScope<Scene<Destination>>.() -> ContentTransform =
        {
            ContentTransform(
                targetContentEnter = fadeIn(tween(250)),
                initialContentExit = fadeOut(tween(250))
            )
        }

    val simpleOverlaySceneStrategy = SimpleOverlaySceneStrategy<Destination>()
    val listDetailStrategy = rememberListDetailSceneStrategy<Destination>()
    val supportingPaneSceneStrategy = rememberSupportingPaneSceneStrategy<Destination>()

    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        sceneStrategies = listOf(
            simpleOverlaySceneStrategy,
            listDetailStrategy,
            supportingPaneSceneStrategy
        ),
        transitionSpec = myTransitionSpec,
        popTransitionSpec = myTransitionSpec,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            // TODO add screens
        }
    )
}