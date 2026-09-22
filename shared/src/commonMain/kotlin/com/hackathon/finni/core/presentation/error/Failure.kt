package com.hackathon.finni.core.presentation.error

import com.hackathon.finni.api.error.ApiError
import com.hackathon.finni.api.error.AuthError
import com.hackathon.finni.api.error.BadGatewayError
import com.hackathon.finni.api.error.BadRequestError
import com.hackathon.finni.api.error.FailedValidation
import com.hackathon.finni.api.error.InternalServerError
import com.hackathon.finni.api.error.InvalidRequest
import com.hackathon.finni.api.error.NotFoundError
import com.hackathon.finni.api.error.ProjectNotFound
import com.hackathon.finni.api.error.UnknownError
import com.hackathon.finni.api.error.UnprocessableEntityError
import com.hackathon.finni.api.error.UserError
import com.hackathon.finni.api.response.ApiException
import com.hackathon.finni.core.network.isNetworkIssue
import com.hackathon.finni.core.presentation.model.UiImage
import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.features.auth.toAuthIcon
import com.hackathon.finni.features.auth.toAuthTitle
import com.hackathon.finni.features.users.toUserIcon
import com.hackathon.finni.features.users.toUserTitle
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.common_error_bad_gateway_title
import com.hackathon.finni.resources.common_error_bad_request_title
import com.hackathon.finni.resources.common_error_internal_server_title
import com.hackathon.finni.resources.common_error_invalid_request_title
import com.hackathon.finni.resources.common_error_no_connection_title
import com.hackathon.finni.resources.common_error_not_found_title
import com.hackathon.finni.resources.common_error_timeout_title
import com.hackathon.finni.resources.common_error_unknown_title
import com.hackathon.finni.resources.common_error_unprocessable_entity_title
import com.hackathon.finni.resources.ic_bug_report
import com.hackathon.finni.resources.ic_dns
import com.hackathon.finni.resources.ic_hourglass_bottom
import com.hackathon.finni.resources.ic_search
import com.hackathon.finni.resources.ic_warning
import com.hackathon.finni.resources.ic_wifi_off
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode


sealed interface Failure

data object NoConnection : Failure

data object Timeout : Failure

data class ApiFailure(
    val error: ApiError
) : Failure {

    val httpStatusCode: HttpStatusCode
        get() = HttpStatusCode.fromValue(error.httpCode.value)
}

data class ThrowableFailure(
    val throwable: Throwable
) : Failure


fun Throwable.asFailure(): Failure = when (this) {
    is FailureException -> failure
    is ApiException -> ApiFailure(error)
    else -> {
        if (isNetworkIssue()) NoConnection
        else ThrowableFailure(this)
    }
}


suspend fun HttpResponse.asFailure(): ApiFailure {
    return ApiFailure(UnknownError(status.value, bodyAsText()))
}

class FailureException(val failure: Failure) : Exception("App failure occurred: $failure")

fun Failure.asThrowable(): Throwable = FailureException(this)


fun Failure.toUiError(
//    onAction: (() -> Unit)? = null,
    localHandler: ((Failure) -> UiError?)? = null
): UiError {
    val defaultUiError = UiError(
        origin = this,
//        onAction = onAction
    )
    return localHandler?.invoke(this) ?: defaultUiError
}

fun Failure.resolveTitle(): UiText {
    return when (this) {
        is NoConnection -> UiText.Resource(Res.string.common_error_no_connection_title)
        Timeout -> UiText.Resource(Res.string.common_error_timeout_title)
        is ApiFailure -> when (val e = this.error) {
            is NotFoundError -> UiText.Resource(Res.string.common_error_not_found_title)
            is UserError -> e.toUserTitle()
            is AuthError -> e.toAuthTitle()
            is BadRequestError.General -> UiText.Resource(Res.string.common_error_bad_request_title)
            is InvalidRequest -> UiText.Resource(Res.string.common_error_invalid_request_title)
            is UnprocessableEntityError -> UiText.Resource(Res.string.common_error_unprocessable_entity_title)
            is BadGatewayError -> UiText.Resource(Res.string.common_error_bad_gateway_title)
            is InternalServerError -> UiText.Resource(Res.string.common_error_internal_server_title)
            is ProjectNotFound -> UiText.Dynamic("Проект не найден")
            is FailedValidation -> UiText.Dynamic("Ошибка валидации")
            is UnknownError -> UiText.Resource(Res.string.common_error_unknown_title)
        }

        is ThrowableFailure -> UiText.Resource(Res.string.common_error_internal_server_title)
    }
}

fun Failure.resolveIcon(): UiImage {
    return when (this) {
        // --- 1. Клиентские системные сбои ---
        NoConnection -> UiImage.Resource(Res.drawable.ic_wifi_off)
        Timeout -> UiImage.Resource(Res.drawable.ic_hourglass_bottom)
        is ThrowableFailure -> UiImage.Resource(Res.drawable.ic_bug_report)

        // --- 2. Разбор серверных ошибок (ApiFailure) ---
        is ApiFailure -> when (val e = this.error) {
            // Маркерные бизнес-интерфейсы
            is AuthError -> e.toAuthIcon()
            is UserError -> e.toUserIcon()
            is NotFoundError -> UiImage.Resource(Res.drawable.ic_search)
            is FailedValidation -> UiImage.Resource(Res.drawable.ic_warning)

            // Системные коды ответов сервера
            is InternalServerError, is BadGatewayError -> UiImage.Resource(Res.drawable.ic_dns)
            is BadRequestError, is UnprocessableEntityError -> UiImage.Resource(Res.drawable.ic_warning)

            // Глобальный фолбэк для неизвестных серверных ошибок
            else -> UiImage.Resource(Res.drawable.ic_warning)
        }
    }
}
