package com.hackathon.finni.features.auth

import com.hackathon.finni.core.presentation.model.UiText

data class RegisterUiState(
    val input: RegisterInput = RegisterInput(),
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val nicknameError: UiText? = null,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val confirmPasswordError: UiText? = null,
    val isLoading: Boolean = false
)

data class RegisterInput(
    val nickname: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)
