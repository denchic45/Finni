package com.hackathon.finni.features.auth

import com.hackathon.finni.api.error.AuthError
import com.hackathon.finni.api.error.EmailAlreadyUse
import com.hackathon.finni.api.error.InvalidCredentials
import com.hackathon.finni.api.error.InvalidRefreshToken
import com.hackathon.finni.api.error.InvalidToken
import com.hackathon.finni.api.error.UnauthorizedError
import com.hackathon.finni.core.presentation.model.UiImage
import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.auth_error_credentials_msg
import com.hackathon.finni.resources.auth_error_session_expired_msg
import com.hackathon.finni.resources.auth_error_title
import com.hackathon.finni.resources.auth_error_unauthorized_msg
import com.hackathon.finni.resources.ic_login
import com.hackathon.finni.resources.user_error_email_already_in_use_msg


fun AuthError.toAuthTitle(): UiText = when (this) {
    else -> UiText.Resource(Res.string.auth_error_title)
}

fun AuthError.toAuthIcon(): UiImage = when (this) {
    else -> UiImage.Resource(Res.drawable.ic_login)
}

fun AuthError.toAuthMessage(): UiText = when (this) {
    InvalidToken, InvalidRefreshToken -> UiText.Resource(Res.string.auth_error_session_expired_msg)
    UnauthorizedError -> UiText.Resource(Res.string.auth_error_unauthorized_msg)
    InvalidCredentials -> UiText.Resource(Res.string.auth_error_credentials_msg)
    is EmailAlreadyUse -> UiText.Resource(Res.string.user_error_email_already_in_use_msg)
}