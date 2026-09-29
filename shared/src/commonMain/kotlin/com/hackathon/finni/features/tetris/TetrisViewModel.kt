package com.hackathon.finni.features.tetris

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.router.pop
import com.hackathon.finni.data.repository.GameStateRepository
import com.hackathon.finni.features.levels.model.LevelStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TetrisUiState(
    val unlocked: Boolean = false,
    val game: TetrisGame = TetrisEngine.newGame(),
    val rewardGranted: Boolean = false
)

class TetrisViewModel(
    private val repository: GameStateRepository,
    private val router: Router
) : ViewModel() {
    private val game = MutableStateFlow(TetrisEngine.newGame())
    private val rewardGranted = MutableStateFlow(false)

    val uiState: StateFlow<TetrisUiState> = combine(repository.levels, game, rewardGranted) { levels, currentGame, rewarded ->
        TetrisUiState(
            unlocked = levels.any { it.number == 10 && it.status == LevelStatus.COMPLETED },
            game = currentGame,
            rewardGranted = rewarded
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TetrisUiState())

    fun onTick() = updateGame(TetrisEngine::tick)
    fun onMoveLeft() = updateGame(TetrisEngine::moveLeft)
    fun onMoveRight() = updateGame(TetrisEngine::moveRight)
    fun onRotate() = updateGame(TetrisEngine::rotate)
    fun onHardDrop() = updateGame(TetrisEngine::hardDrop)

    fun onPauseToggle() {
        game.value = game.value.let {
            when (it.status) {
                TetrisStatus.PLAYING -> it.copy(status = TetrisStatus.PAUSED)
                TetrisStatus.PAUSED -> it.copy(status = TetrisStatus.PLAYING)
                else -> it
            }
        }
    }

    fun onNewGame() {
        rewardGranted.value = false
        game.value = TetrisEngine.newGame()
    }

    fun onBack() = router.pop()

    private fun updateGame(transform: (TetrisGame) -> TetrisGame) {
        val old = game.value
        val updated = transform(old)
        game.value = updated
        if (old.status != TetrisStatus.WON && updated.status == TetrisStatus.WON && !rewardGranted.value) {
            rewardGranted.value = true
            viewModelScope.launch { repository.addCoins(3) }
        }
    }
}
