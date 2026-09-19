package com.hackathon.finni.core.util

import co.touchlab.kermit.Logger

inline fun <reified T : Any> T.createLogger(parentLogger: Logger? = null): Logger {
    val className = T::class.simpleName ?: "Unknown"

    return if (parentLogger != null && parentLogger.tag.isNotEmpty()) {
        val compositeTag = "${parentLogger.tag}:$className"
        parentLogger.withTag(compositeTag)
    } else {
        Logger.withTag(className)
    }
}

val AuthLogger = Logger.withTag("auth")
val NoteLogger = Logger.withTag("note")