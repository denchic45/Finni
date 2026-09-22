package com.hackathon.finni.core.ui.components

actual class ToastManager {
    actual fun showToast(message: String, toastDuration: ToastDuration) {
        println("[Toast] $message (${toastDuration.name})")
    }
}
