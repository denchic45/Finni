package com.hackathon.finni.core

import androidx.compose.runtime.compositionLocalOf

class DesktopBackDispatcher {
    private val handlers = mutableListOf<() -> Unit>()

    fun register(handler: () -> Unit) {
        handlers.add(handler)
    }

    fun unregister(handler: () -> Unit) {
        handlers.remove(handler)
    }

    fun onBackPressed(): Boolean {
        // Вызываем последний зарегистрированный активный обработчик
        val lastHandler = handlers.lastOrNull()
        return if (lastHandler != null) {
            lastHandler.invoke()
            true
        } else {
            false
        }
    }
}

val LocalBackDispatcher = compositionLocalOf { DesktopBackDispatcher() }