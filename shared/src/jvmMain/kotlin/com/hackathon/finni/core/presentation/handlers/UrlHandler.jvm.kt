package com.hackathon.finni.core.presentation.handlers

import java.awt.Desktop
import java.net.URI

actual class UrlHandler {
    actual fun openUrl(url: String) {
        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(URI(url))
                } catch (e: Exception) {
                    // Log error if needed
                }
            }
        }
    }
}
