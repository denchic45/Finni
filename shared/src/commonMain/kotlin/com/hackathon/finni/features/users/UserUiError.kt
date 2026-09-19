package com.hackathon.finni.features.users

import com.hackathon.finni.api.error.UserError
import com.hackathon.finni.api.error.UserNotFound
import com.hackathon.finni.core.presentation.model.UiImage
import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_person_search
import com.hackathon.finni.resources.user_error_not_found_msg
import com.hackathon.finni.resources.user_error_not_found_title

fun UserError.toUserTitle(): UiText = when (this) {
    is UserNotFound -> UiText.Resource(Res.string.user_error_not_found_title)
}

fun UserError.toUserIcon(): UiImage = when (this) {
    is UserNotFound -> UiImage.Resource(Res.drawable.ic_person_search)
}

fun UserError.toUserMessage(): UiText = when (this) {
    is UserNotFound -> UiText.Resource(Res.string.user_error_not_found_msg)
}