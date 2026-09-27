package com.hackathon.finni.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.finni.core.ui.navigation.Home
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.router.pop
import com.hackathon.finni.data.storage.AppSettingsStorage
import com.hackathon.finni.data.storage.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val router: Router,
    private val appSettingsStorage: AppSettingsStorage
) : ViewModel() {


    val navigationState = router.state

    val themeMode = appSettingsStorage.settings
        .map { it.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    init {
        router.switchTab(Home)
    }

    fun onBack() {
        router.pop()
    }
}
