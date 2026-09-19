package com.hackathon.finni.core.presentation.handlers

import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.common_loading_long_msg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


interface LoadingHandler {
    val loading: StateFlow<LoadingState?>

    fun showLoading(
        state: LoadingState = LoadingState.Overlay
    )

    fun showDelayedLoading(
        coroutineScope: CoroutineScope,
        state: LoadingState = LoadingState.Dialog(UiText.Resource(Res.string.common_loading_long_msg))
    )

    fun hideLoading()
}

class LoadingHandlerImpl : LoadingHandler {
    private var loadingJob: Job? = null

    override val loading: StateFlow<LoadingState?>
        field = MutableStateFlow<LoadingState?>(null)

    override fun showDelayedLoading(coroutineScope: CoroutineScope, state: LoadingState) {
        if (loadingJob?.isActive == true) return

        loadingJob = coroutineScope.launch {
            delay(5.seconds)
            loading.value = state
        }
    }

    override fun showLoading(state: LoadingState) {
        cancelPendingJob()
        loading.value = state
    }

    override fun hideLoading() {
        loadingJob?.cancel()
        loadingJob = null
        loading.value = null
    }

    private fun cancelPendingJob() {
        loadingJob?.cancel()
        loadingJob = null
    }
}

sealed interface LoadingState {
    data class Dialog(
        val message: UiText,
        val subMessage: UiText? = null,
        val progress: Float? = null, // null = Indeterminate, 0.0f..1.0f = Determinate
        val cancelActionLabel: UiText? = null
    ) : LoadingState

    data class Snackbar(
        val message: UiText,
        val progress: Float? = null, // null = Indeterminate, 0.0f..1.0f = Determinate
        val cancelActionLabel: UiText? = null
    ) : LoadingState

    data object Overlay : LoadingState
}