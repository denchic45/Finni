package com.hackathon.finni.core.presentation.error

import com.hackathon.finni.api.error.ApiError
import com.hackathon.finni.api.error.AuthError
import com.hackathon.finni.api.error.BadGatewayError
import com.hackathon.finni.api.error.BadRequestError
import com.hackathon.finni.api.error.InternalServerError
import com.hackathon.finni.api.error.InvalidRequest
import com.hackathon.finni.api.error.NotFoundError
import com.hackathon.finni.api.error.UnknownError
import com.hackathon.finni.api.error.UnprocessableEntityError
import com.hackathon.finni.api.error.UserError
import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.features.auth.toAuthMessage
import com.hackathon.finni.features.users.toUserMessage
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.common_error_bad_gateway_msg
import com.hackathon.finni.resources.common_error_internal_server_msg
import com.hackathon.finni.resources.common_error_invalid_request_msg
import com.hackathon.finni.resources.common_error_not_found_msg
import com.hackathon.finni.resources.common_error_unprocessable_entity_msg


fun ApiError.toApiErrorMessage(): UiText = when (this) {
    is AuthError -> this.toAuthMessage()
    is UserError -> this.toUserMessage()

    is InternalServerError -> UiText.Resource(Res.string.common_error_internal_server_msg)
    is InvalidRequest -> UiText.Resource(Res.string.common_error_invalid_request_msg)
    is BadRequestError -> UiText.Dynamic("Ошибка в запросе: $message")
    is NotFoundError -> UiText.Resource(Res.string.common_error_not_found_msg)
    is UnprocessableEntityError -> UiText.Resource(Res.string.common_error_unprocessable_entity_msg)
    is BadGatewayError -> UiText.Resource(Res.string.common_error_bad_gateway_msg)

    is UnknownError -> UiText.Dynamic("Произошла непредвиденная ошибка (Код: ${this.httpCodeValue})")
}
