package com.hackathon.finni.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.hackathon.finni.core.ui.navigation.Auth
import com.hackathon.finni.core.ui.navigation.Home
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.router.pop
import com.hackathon.finni.data.service.AuthService
import com.hackathon.finni.data.storage.AppSettingsStorage
import com.hackathon.finni.data.storage.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val logger: Logger,
    authService: AuthService,
    private val router: Router,
    private val appSettingsStorage: AppSettingsStorage
) : ViewModel() {

    val isAuthorized: StateFlow<Boolean?> = authService.isAuthorized
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val navigationState = router.state

    val themeMode = appSettingsStorage.settings
        .map { it.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    init {
        isAuthorized.filterNotNull()
            .distinctUntilChanged()
            .onEach { authorized ->
                router.switchTab(if (authorized) Home else Auth)
            }.launchIn(viewModelScope)
    }

    fun onBack() {
        router.pop()
    }
}
