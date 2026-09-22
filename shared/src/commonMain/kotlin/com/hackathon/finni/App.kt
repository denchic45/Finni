package com.hackathon.finni

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.hackathon.finni.core.presentation.MainViewModel
import com.hackathon.finni.core.theme.AppTheme
import com.hackathon.finni.core.ui.components.AppEventHandlerHost
import com.hackathon.finni.core.ui.components.LoadingHost
import com.hackathon.finni.core.ui.navigation.AppNavDisplay
import com.hackathon.finni.core.ui.navigation.Auth
import com.hackathon.finni.core.ui.navigation.Confirmation
import com.hackathon.finni.core.ui.navigation.GameUiShowcase
import com.hackathon.finni.core.ui.navigation.Home
import com.hackathon.finni.core.ui.navigation.NoteEditor
import com.hackathon.finni.core.ui.navigation.OverlayImages
import com.hackathon.finni.core.ui.navigation.ProjectEditor
import com.hackathon.finni.core.ui.navigation.Register
import com.hackathon.finni.core.ui.navigation.SimpleOverlaySceneStrategy
import com.hackathon.finni.core.ui.navigation.Splash
import com.hackathon.finni.core.ui.navigation.TagEditor
import com.hackathon.finni.core.ui.navigation.router.Destination
import com.hackathon.finni.core.ui.overlay.OverlayImagesScreen
import com.hackathon.finni.data.storage.ThemeMode
import com.hackathon.finni.features.auth.AuthScreen
import com.hackathon.finni.features.auth.RegisterScreen
import com.hackathon.finni.features.game_ui_showcase.GameUiShowcaseScreen
import com.hackathon.finni.features.home.HomeScreen
import com.hackathon.finni.features.noteeditor.NoteEditorScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    viewModel: MainViewModel = koinViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val navigationState by viewModel.navigationState.collectAsState()

    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    AppTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavigation(
                backStack = navigationState.flattenedBackStack,
                onBack = viewModel::onBack
            )
        }
    }

    AppEventHandlerHost()
    LoadingHost()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AppNavigation(
    backStack: List<Destination>,
    onBack: () -> Unit
) {
    val simpleOverlaySceneStrategy = SimpleOverlaySceneStrategy<Destination>()
    val listDetailStrategy = rememberListDetailSceneStrategy<Destination>()
    val supportingPaneSceneStrategy = rememberSupportingPaneSceneStrategy<Destination>()

    AppNavDisplay(
        backstack = backStack,
        onBack = onBack,
        sceneStrategies = listOf(
            simpleOverlaySceneStrategy,
            listDetailStrategy,
            supportingPaneSceneStrategy
        ),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Splash> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            entry<Auth> {
                AuthScreen()
            }
            entry<Register> {
                RegisterScreen()
            }
            entry<Home>(metadata = ListDetailSceneStrategy.listPane()) {
                HomeScreen()
            }
            entry<NoteEditor>(metadata = ListDetailSceneStrategy.detailPane()) { details: NoteEditor ->
                NoteEditorScreen(details.noteId)
            }
            entry<ProjectEditor>(metadata = SimpleOverlaySceneStrategy.overlay()) { editor: ProjectEditor ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Project Editor: ${editor.projectId}")
                }
            }
            entry<TagEditor>(metadata = SimpleOverlaySceneStrategy.overlay()) { editor: TagEditor ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tag Editor: ${editor.tagId}")
                }
            }
            entry<Confirmation>(metadata = SimpleOverlaySceneStrategy.overlay()) { conf: Confirmation ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Confirmation: ${conf.title}")
                }
            }
            entry<OverlayImages>(metadata = SimpleOverlaySceneStrategy.overlay()) { images: OverlayImages ->
                OverlayImagesScreen(images.urls, images.initialIndex)
            }
            entry<GameUiShowcase> {
                GameUiShowcaseScreen(
                    onBack = onBack
                )
            }
        }
    )
}
