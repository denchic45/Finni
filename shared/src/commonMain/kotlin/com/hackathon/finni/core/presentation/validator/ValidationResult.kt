package com.hackathon.finni.core.presentation.validator

fun interface ValidationResult {
    operator fun invoke(isValid: Boolean)
}