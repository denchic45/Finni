package com.hackathon.finni.features.auth

import androidx.lifecycle.ViewModel
import com.hackathon.finni.api.profile.model.LoginRequest
import com.hackathon.finni.core.presentation.handlers.ErrorHandler
import com.hackathon.finni.core.presentation.handlers.LoadingMode
import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.core.presentation.validator.Operator
import com.hackathon.finni.core.presentation.validator.compositeValidator
import com.hackathon.finni.core.presentation.validator.isEmail
import com.hackathon.finni.core.ui.navigation.Register
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.router.push
import com.hackathon.finni.data.service.AuthService
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.auth_email_error_empty
import com.hackathon.finni.resources.auth_email_error_invalid
import com.hackathon.finni.resources.auth_password_error_empty
import com.hackathon.finni.resources.auth_password_error_short
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val input: AuthInput = AuthInput(),
    val isPasswordVisible: Boolean = false,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val isLoading: Boolean = false
)

data class AuthInput(
    val email: String = "",
    val password: String = ""
)

class AuthViewModel(
    private val authService: AuthService,
    private val errorHandler: ErrorHandler,
    private val router: Router
) : ViewModel(), ErrorHandler by errorHandler {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val formValidator = compositeValidator {
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
                passwordError = null
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onRegisterClick() {
        router.push(Register)
    }

    fun onLoginClick() {
        formValidator.onValid {
            val input = _uiState.value.input
            launchSafe(loadingMode = LoadingMode.None) {
                _uiState.update { it.copy(isLoading = true) }
                val result = authService.login(
                    LoginRequest(
                        email = input.email.trim(),
                        password = input.password
                    )
                )
                _uiState.update { it.copy(isLoading = false) }
                result
            }
        }
    }
}
