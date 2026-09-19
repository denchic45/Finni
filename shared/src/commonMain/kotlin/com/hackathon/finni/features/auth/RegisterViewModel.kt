package com.hackathon.finni.features.auth

import androidx.lifecycle.ViewModel
import com.hackathon.finni.api.profile.model.RegisterRequest
import com.hackathon.finni.core.presentation.handlers.ErrorHandler
import com.hackathon.finni.core.presentation.handlers.LoadingMode
import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.core.presentation.validator.Operator
import com.hackathon.finni.core.presentation.validator.compositeValidator
import com.hackathon.finni.core.presentation.validator.isEmail
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.router.pop
import com.hackathon.finni.data.service.AuthService
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.auth_email_error_empty
import com.hackathon.finni.resources.auth_email_error_invalid
import com.hackathon.finni.resources.auth_password_error_empty
import com.hackathon.finni.resources.auth_password_error_short
import com.hackathon.finni.resources.register_confirm_password_error_empty
import com.hackathon.finni.resources.register_confirm_password_error_mismatch
import com.hackathon.finni.resources.register_nickname_error_empty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegisterViewModel(
    private val authService: AuthService,
    private val errorHandler: ErrorHandler,
    private val router: Router
) : ViewModel(), ErrorHandler by errorHandler {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val formValidator = compositeValidator {
        // Nickname
        value(
            provider = { _uiState.value.input.nickname.trim() },
            operator = Operator.all()
        ) {
            onResult { isValid ->
                if (isValid) {
                    _uiState.update { it.copy(nicknameError = null) }
                }
            }
            check({ it.isNotBlank() }) { isValid ->
                if (!isValid) {
                    _uiState.update { it.copy(nicknameError = UiText.Resource(Res.string.register_nickname_error_empty)) }
                }
            }
        }

        // Email
        value(
            provider = { _uiState.value.input.email.trim() },
            operator = Operator.all()
        ) {
            onResult { isValid ->
                if (isValid) {
                    _uiState.update { it.copy(emailError = null) }
                }
            }
            check({ it.isNotBlank() }) { isValid ->
                if (!isValid) {
                    _uiState.update { it.copy(emailError = UiText.Resource(Res.string.auth_email_error_empty)) }
                }
            }
            check({ it.isEmail() }) { isValid ->
                if (!isValid) {
                    _uiState.update { it.copy(emailError = UiText.Resource(Res.string.auth_email_error_invalid)) }
                }
            }
        }

        // Password
        value(
            provider = { _uiState.value.input.password },
            operator = Operator.all()
        ) {
            onResult { isValid ->
                if (isValid) {
                    _uiState.update { it.copy(passwordError = null) }
                }
            }
            check({ it.isNotBlank() }) { isValid ->
                if (!isValid) {
                    _uiState.update { it.copy(passwordError = UiText.Resource(Res.string.auth_password_error_empty)) }
                }
            }
            check({ it.length >= 6 }) { isValid ->
                if (!isValid) {
                    _uiState.update { it.copy(passwordError = UiText.Resource(Res.string.auth_password_error_short)) }
                }
            }
        }

        // Confirm Password
        value(
            provider = { _uiState.value.input.confirmPassword },
            operator = Operator.all()
        ) {
            onResult { isValid ->
                if (isValid) {
                    _uiState.update { it.copy(confirmPasswordError = null) }
                }
            }
            check({ it.isNotBlank() }) { isValid ->
                if (!isValid) {
                    _uiState.update { it.copy(confirmPasswordError = UiText.Resource(Res.string.register_confirm_password_error_empty)) }
                }
            }
            check({ it == _uiState.value.input.password }) { isValid ->
                if (!isValid) {
                    _uiState.update { it.copy(confirmPasswordError = UiText.Resource(Res.string.register_confirm_password_error_mismatch)) }
                }
            }
        }
    }

    fun onNicknameChange(nickname: String) {
        _uiState.update {
            it.copy(
                input = it.input.copy(nickname = nickname),
                nicknameError = null
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                input = it.input.copy(email = email),
                emailError = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                input = it.input.copy(password = password),
                passwordError = null,
                confirmPasswordError = if (it.input.confirmPassword.isNotEmpty() && it.input.confirmPassword != password) {
                    it.confirmPasswordError
                } else null
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update {
            it.copy(
                input = it.input.copy(confirmPassword = confirmPassword),
                confirmPasswordError = null
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onLoginClick() {
        router.pop()
    }

    fun onRegisterClick() {
        formValidator.onValid {
            val input = _uiState.value.input
            launchSafe(loadingMode = LoadingMode.None) {
                _uiState.update { it.copy(isLoading = true) }
                val result = authService.register(
                    RegisterRequest(
                        email = input.email.trim(),
                        password = input.password,
                        nickname = input.nickname.trim()
                    )
                )
                _uiState.update { it.copy(isLoading = false) }
                result
            }
        }
    }
}
