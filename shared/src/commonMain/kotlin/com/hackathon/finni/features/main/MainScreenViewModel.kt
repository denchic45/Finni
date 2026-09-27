package com.hackathon.finni.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.finni.core.ui.components.game.GameTab
import com.hackathon.finni.core.ui.components.game.GameTimePhase
import com.hackathon.finni.core.ui.components.game.PetMood
import com.hackathon.finni.core.ui.navigation.LevelsScreen
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.router.push
import com.hackathon.finni.data.repository.GameStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainScreenUiState(
    val coins: Int = 1150,
    val hunger: Int = 4,
    val mood: PetMood = PetMood.Happy,
    val timePhase: GameTimePhase = GameTimePhase.Day,
    val isTasksDialogOpen: Boolean = false,
    val selectedTab: GameTab? = null
)

class MainScreenViewModel(
    private val repository: GameStateRepository,
    private val router: Router
) : ViewModel() {

    private val _isTasksDialogOpen = MutableStateFlow(false)
    private val _selectedTab = MutableStateFlow<GameTab?>(null)

    val uiState: StateFlow<MainScreenUiState> = combine(
        repository.petState,
        repository.accountState,
        _isTasksDialogOpen,
        _selectedTab
    ) { pet, account, isTasksOpen, tab ->
        MainScreenUiState(
            coins = account.walletCoins,
            hunger = pet.hunger,
            mood = when (pet.mood) {
                "Happy" -> PetMood.Happy
                "Sad" -> PetMood.Sad
                else -> PetMood.Neutral
            },
            timePhase = when (pet.timePhase) {
                "Morning" -> GameTimePhase.Morning
                "Day" -> GameTimePhase.Day
                "Evening" -> GameTimePhase.Evening
                else -> GameTimePhase.Night
            },
            isTasksDialogOpen = isTasksOpen,
            selectedTab = tab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainScreenUiState()
    )

    fun onBowlClick() {
        viewModelScope.launch {
            repository.feedPet(cost = 10, hungerReplenish = 1)
        }
    }

    fun onMoodClick() {
        viewModelScope.launch {
            val nextMood = when (uiState.value.mood) {
                PetMood.Happy -> "Neutral"
                PetMood.Neutral -> "Sad"
                PetMood.Sad -> "Happy"
            }
            repository.updateMood(nextMood)
        }
    }

    fun onTabSelected(tab: GameTab) {
        _selectedTab.value = tab
        when (tab) {
            GameTab.Levels -> {
                router.push(LevelsScreen)
            }

            GameTab.Tasks -> {
                _isTasksDialogOpen.value = true
            }

            GameTab.Piggy, GameTab.Shop -> {
                // Вкладки в разработке
            }
        }
    }

    fun onDismissTasksDialog() {
        _isTasksDialogOpen.value = false
        if (_selectedTab.value == GameTab.Tasks) {
            _selectedTab.value = null
        }
    }

    fun onAdvanceTimePhase() {
        viewModelScope.launch {
            repository.advanceTimePhase()
        }
    }
}
