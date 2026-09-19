package com.hackathon.finni.core.ui.components

expect class ToastManager {
    fun showToast(message: String, toastDuration: ToastDuration)
}

enum class ToastDuration(val millis: Long) {
    SHORT(2000L),
    LONG(4000L)
}