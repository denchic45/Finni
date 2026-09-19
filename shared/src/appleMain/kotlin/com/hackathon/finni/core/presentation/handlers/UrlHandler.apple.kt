package com.hackathon.finni.core.presentation.handlers

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class UrlHandler {
    actual fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }
}