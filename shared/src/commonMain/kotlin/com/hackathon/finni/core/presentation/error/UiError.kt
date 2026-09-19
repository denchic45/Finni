package com.hackathon.finni.core.presentation.error

import com.hackathon.finni.core.presentation.model.UiImage
import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.common_action_retry

enum class ErrorSnackbarDuration {
    SHORT,
    LONG,
    INDEFINITE
}

data class UiError(
    val origin: Failure,
    val message: UiText = origin.getErrorMessage(),
    val title: UiText = origin.resolveTitle(),
    val icon: UiImage = origin.resolveIcon(),
    val actionText: UiText = UiText.Resource(Res.string.common_action_retry),
    val actionDisplayType: ActionErrorDisplayType = origin.resolveDefaultDisplayType(),
)