package com.hackathon.finni.core.presentation.handlers

import com.hackathon.finni.api.error.UnauthorizedError
import com.hackathon.finni.core.presentation.error.ActionErrorDisplayType
import com.hackathon.finni.core.presentation.error.UiError
import com.hackathon.finni.core.presentation.model.UiImage
import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.core.ui.components.ToastDuration
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.auth_action_login
import com.hackathon.finni.resources.common_action_cancel
import com.hackathon.finni.resources.common_action_ok
import com.hackathon.finni.resources.common_action_retry
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class EventHandler(private val router: Router) {
    private val _events = Channel<UIEvent>(Channel.BUFFERED)
    val events: Flow<UIEvent> = _events.receiveAsFlow()


    suspend fun handleUiError(uiError: UiError, onRetry: (() -> Unit)? = null) {
        handleUiError(uiError, onRetry, ::defaultHandle)
    }

    suspend fun handleUiError(
        uiError: UiError,
        onRetry: (() -> Unit)? = null,
        onHandle: (UiError) -> UIEvent = { defaultHandle(uiError, onRetry) }
    ) {
        _events.send(onHandle(uiError))
    }

    fun defaultHandle(
        uiError: UiError,
        onRetry: (() -> Unit)? = null
    ): UIEvent {
        val defaultAction = when {
            uiError.origin is UnauthorizedError -> UIEvent.Action(
                UiText.Resource(Res.string.auth_action_login)
            ) { router.updateTabs { mapOf() } }

            onRetry != null -> UIEvent.Action(
                UiText.Resource(Res.string.common_action_retry),
                onRetry
            )

            else -> null
        }

        return when (uiError.actionDisplayType) {
            is ActionErrorDisplayType.Dialog -> UIEvent.AlertMessage(
                title = uiError.title,
                text = uiError.message,
                icon = uiError.icon,
                confirmAction = defaultAction ?: UIEvent.Action.DEFAULT_CONFIRM_ACTION
            )

            is ActionErrorDisplayType.Snackbar -> UIEvent.Snackbar(
                message = uiError.message,
                action = defaultAction
            )
        }
    }

    suspend fun sendEvent(event: UIEvent) {
        _events.send(event)
    }
}

sealed interface UIEvent {

    data class AlertMessage(
        val title: UiText,
        val text: UiText? = null,
        val icon: UiImage? = null,
        val confirmAction: Action = Action.DEFAULT_CONFIRM_ACTION,
        val dismissAction: Action? = if (confirmAction != Action.DEFAULT_CONFIRM_ACTION)
            Action(UiText.Resource(Res.string.common_action_cancel)) {} else null
    ) : UIEvent

    data class Action(val label: UiText, val onClick: () -> Unit) {
        companion object {
            val DEFAULT_CONFIRM_ACTION = Action(UiText.Resource(Res.string.common_action_ok)) {}
        }
    }

    data class Toast(
        val message: UiText,
        val duration: ToastDuration = ToastDuration.SHORT
    ) : UIEvent

    data class Snackbar(val message: UiText, val action: Action? = null) : UIEvent
}