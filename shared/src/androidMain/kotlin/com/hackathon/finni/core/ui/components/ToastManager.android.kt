package com.hackathon.finni.core.ui.components

import android.content.Context
import android.widget.Toast

actual class ToastManager(private val context: Context) {
    actual fun showToast(message: String, toastDuration: ToastDuration) {
        val duration = when (toastDuration) {
            ToastDuration.SHORT -> Toast.LENGTH_SHORT
            ToastDuration.LONG -> Toast.LENGTH_LONG
        }
        Toast.makeText(context, message, duration).show()
    }
}