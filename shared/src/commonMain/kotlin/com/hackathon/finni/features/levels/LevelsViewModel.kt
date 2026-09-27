package com.hackathon.finni.features.levels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.router.pop
import com.hackathon.finni.data.repository.GameStateRepository
import com.hackathon.finni.features.levels.model.LevelItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LevelsUiState(
    val levels: List<LevelItem> = emptyList(),
    val coins: Int = 1150,
    val selectedLevelForModal: LevelItem? = null
)

class LevelsViewModel(
    private val repository: GameStateRepository,
    private val router: Router
) : ViewModel() {

    private val _selectedLevel = kotlinx.coroutines.flow.MutableStateFlow<LevelItem?>(null)

    val uiState: StateFlow<LevelsUiState> = combine(
        repository.levels,
        repository.accountState,
        _selectedLevel
    ) { levels, account, selected ->
        LevelsUiState(
            levels = levels,
            coins = account.walletCoins,
            selectedLevelForModal = selected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LevelsUiState()
    )

    fun onHomeClick() {
        router.pop()
    }

    fun onLevelClick(level: LevelItem) {
        _selectedLevel.value = level
    }

    fun onDismissModal() {
        _selectedLevel.value = null
    }

    fun onCompleteLevel(level: LevelItem, stars: Int = 3) {
        viewModelScope.launch {
            repository.completeLevel(level.id, stars, level.rewardCoins)
            _selectedLevel.value = null
        }
    }
}
