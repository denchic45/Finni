package com.hackathon.finni.core.presentation.handlers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.finni.core.presentation.error.Failure
import com.hackathon.finni.core.presentation.error.UiError
import com.hackathon.finni.core.presentation.error.toUiError
import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.data.RequestResult
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.common_loading_long_msg
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface ErrorHandler {

    context(viewModel: ViewModel)
    fun launchSafe(
        loadingMode: LoadingMode = LoadingMode.Delayed(),
        failureMapper: ((Failure) -> UiError?)? = null,
        handler: (suspend (UiError) -> Boolean)? = null,
        block: suspend () -> RequestResult<*>
    ): Job
}

class ErrorHandlerImpl(
    private val eventHandler: EventHandler,
    private val loadingHandler: LoadingHandler
) : ErrorHandler {

    context(viewModel: ViewModel)
    override fun launchSafe(
        loadingMode: LoadingMode,
        failureMapper: ((Failure) -> UiError?)?,
        handler: (suspend (UiError) -> Boolean)?,
        block: suspend () -> RequestResult<*>
    ): Job = viewModel.viewModelScope.launch {
        when (loadingMode) {
            is LoadingMode.Instant -> loadingHandler.showLoading(loadingMode.state)
            is LoadingMode.Delayed -> loadingHandler.showDelayedLoading(
                coroutineScope = this,
                state = loadingMode.state
            )

            LoadingMode.None -> Unit
        }

        block().onLeft { failure ->
            val uiError = failure.toUiError(localHandler = failureMapper)
            val isHandledLocally = handler?.invoke(uiError) == true

            if (!isHandledLocally) {
                eventHandler.handleUiError(
                    uiError = uiError,
                    onRetry = { launchSafe(loadingMode, failureMapper, handler, block) }
                )
            }
        }

        if (loadingMode !is LoadingMode.None) {
            loadingHandler.hideLoading()
        }
    }
}

sealed interface LoadingMode {
    /**
     * Без показа лоадера (например, для Pull-To-Refresh или фонового тика).
     */
    data object None : LoadingMode

    /**
     * Мгновенный показ лоадера (блокирующий оверлей или диалог).
     */
    data class Instant(
        val state: LoadingState = LoadingState.Overlay
    ) : LoadingMode

    /**
     * Показ лоадера с задержкой (по умолчанию 5 секунд), чтобы не моргать UI при быстрых запросах.
     */
    data class Delayed(
        val state: LoadingState = LoadingState.Dialog(UiText.Resource(Res.string.common_loading_long_msg))
    ) : LoadingMode
}
