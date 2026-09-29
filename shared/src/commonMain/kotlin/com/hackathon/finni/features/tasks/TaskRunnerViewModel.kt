package com.hackathon.finni.features.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.finni.core.presentation.handlers.ErrorHandler
import com.hackathon.finni.core.presentation.handlers.LoadingMode
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.router.pop
import com.hackathon.finni.data.database.entity.TaskCompletionEntity
import com.hackathon.finni.data.repository.GameStateRepository
import com.hackathon.finni.data.repository.TaskSubmission
import com.hackathon.finni.data.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class TaskRunnerUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val wallet: Int = 0,
    val savings: Int = 0,
    val hunger: Int = 0,
    val tokens: Int = 0,
    val selectedChoice: DilemmaChoice? = null,
    val completion: TaskCompletionEntity? = null,
    val blocked: TaskBlockReason? = null,
    val tetrisUnlockedNow: Boolean = false
)

class TaskRunnerViewModel(
    private val tasksRepository: TasksRepository,
    private val gameStateRepository: GameStateRepository,
    private val router: Router,
    errorHandler: ErrorHandler
) : ViewModel(), ErrorHandler by errorHandler {
    private val _taskId = MutableStateFlow(DilemmaCatalog.ROBOT_ID)
    private var levelId: Int? = null
    val task: DilemmaTask
        get() = DilemmaCatalog.byId(_taskId.value) ?: DilemmaCatalog.brokenRobot
    private val _uiState = MutableStateFlow(TaskRunnerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        combine(gameStateRepository.accountState, gameStateRepository.petState, _taskId.flatMapLatest(tasksRepository::completion)) {
            account, pet, completion ->
            Triple(account, pet, completion)
        }.onEach { (account, pet, completion) ->
            _uiState.update {
                it.copy(
                    isLoading = false, wallet = account.walletCoins, savings = account.savingsCoins,
                    hunger = pet.hunger, tokens = pet.timeTokens, completion = completion
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onTaskOpened(taskId: String, levelId: Int? = null) {
        this.levelId = levelId
        if (DilemmaCatalog.byId(taskId) != null && _taskId.value != taskId) {
            _taskId.value = taskId
            _uiState.value = TaskRunnerUiState()
        }
    }

    fun onChoiceSelected(choiceId: String) {
        val state = _uiState.value
        if (state.isLoading || state.isSaving || state.completion != null) return
        val choice = task.choices.find { it.id == choiceId } ?: return
        when (val evaluation = evaluateDilemma(choice.effect, state.savings, state.hunger, state.tokens)) {
            is DilemmaEvaluation.Accepted -> _uiState.update { it.copy(selectedChoice = choice, blocked = null) }
            is DilemmaEvaluation.Blocked -> _uiState.update { it.copy(blocked = evaluation.reason) }
        }
    }

    fun onConfirmationDismissed() {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(selectedChoice = null) }
    }

    fun onConfirmChoice() {
        val state = _uiState.value
        val choice = state.selectedChoice ?: return
        if (state.isSaving || state.completion != null) return
        _uiState.update { it.copy(isSaving = true) }
        launchSafe(loadingMode = LoadingMode.None) {
            _uiState.update { it.copy(isSaving = true) }
            try {
                tasksRepository.complete(task.id, choice.id).onRight { result ->
                    _uiState.update {
                        when (result) {
                            is TaskSubmission.Completed -> it.copy(completion = result.completion, selectedChoice = null, blocked = null)
                            is TaskSubmission.Blocked -> it.copy(blocked = result.reason, selectedChoice = null)
                        }
                    }
                    if (result is TaskSubmission.Completed && result.isNew) {
                        levelId?.let { completedLevelId ->
                            val level = gameStateRepository.levels.first().find { it.id == completedLevelId }
                            if (level != null) {
                                gameStateRepository.completeLevel(level.id, stars = 3, rewardCoins = level.rewardCoins)
                                if (completedLevelId == 10) {
                                    _uiState.update { it.copy(tetrisUnlockedNow = true) }
                                }
                            }
                        }
                    }
                }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onBack() {
        if (!_uiState.value.isSaving) router.pop()
    }

    fun onTetrisUnlockMessageDismissed() {
        _uiState.update { it.copy(tetrisUnlockedNow = false) }
    }
}
