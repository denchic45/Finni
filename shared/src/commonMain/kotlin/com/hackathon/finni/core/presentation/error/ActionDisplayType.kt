package com.hackathon.finni.core.presentation.error

import com.hackathon.finni.api.error.AuthError
import com.hackathon.finni.api.error.BadGatewayError
import com.hackathon.finni.api.error.InternalServerError

sealed interface ActionErrorDisplayType {
    data class Snackbar(
        val duration: ErrorSnackbarDuration = ErrorSnackbarDuration.SHORT,
        val withCloseButton: Boolean = false
    ) : ActionErrorDisplayType

    data class Dialog(
        val isCancelable: Boolean = true
    ) : ActionErrorDisplayType
}

/**
 * Определяет способ отображения ошибки по умолчанию на основе её категории и тяжести.
 */
fun Failure.resolveDefaultDisplayType(): ActionErrorDisplayType {
    return when (this) {
        // 1. Ошибки сети и таймауты — показываем быстрым Снэкбаром
        is NoConnection,
        is Timeout -> ActionErrorDisplayType.Snackbar(
            duration = ErrorSnackbarDuration.SHORT
        )

        // 2. Неизвестные исключения / Краши — делаем Снэкбар с увеличенным таймаутом и кнопкой закрытия
        is ThrowableFailure -> ActionErrorDisplayType.Snackbar(
            duration = ErrorSnackbarDuration.LONG,
            withCloseButton = true
        )

        // 3. API Ошибки сервера и фич
        is ApiFailure -> when (val domainError = this.error) {
            // Ошибки авторизации требует фокуса пользователя — открываем Dialog
            is AuthError -> ActionErrorDisplayType.Dialog(
                isCancelable = false // Требуем явного нажатия кнопки (например, "Войти")
            )

            // Тяжелые ошибки сервера (500, 502) — показываем незакрываемый диалог или длинный Снэкбар
            is InternalServerError,
            is BadGatewayError -> ActionErrorDisplayType.Dialog(
                isCancelable = true
            )

            // По умолчанию для всех оставшихся бизнес-ошибок фич — стандартный Snackbar
            else -> ActionErrorDisplayType.Snackbar(
                duration = ErrorSnackbarDuration.SHORT
            )
        }
    }
}