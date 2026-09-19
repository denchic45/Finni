package com.hackathon.finni.core.presentation.error

import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.common_error_no_connection_msg
import com.hackathon.finni.resources.common_error_timeout_msg
import com.hackathon.finni.resources.common_error_unknown_msg

fun Failure.getErrorMessage(): UiText = when (this) {
    NoConnection -> UiText.Resource(Res.string.common_error_no_connection_msg)
    Timeout -> UiText.Resource(Res.string.common_error_timeout_msg)
    is ApiFailure -> getApiErrorMessage()
    is ThrowableFailure -> UiText.Resource(Res.string.common_error_unknown_msg)
}

fun Failure.getErrorMessage(handler: () -> UiText?) = handler() ?: getErrorMessage()

fun ApiFailure.getApiErrorMessage(): UiText = error.toApiErrorMessage()
