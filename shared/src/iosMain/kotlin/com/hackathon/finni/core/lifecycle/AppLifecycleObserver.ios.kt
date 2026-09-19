package com.hackathon.finni.core.lifecycle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification

actual class AppLifecycleObserver {

    private val _state = MutableStateFlow(AppLifecycleState.FOREGROUND)
    actual val state: StateFlow<AppLifecycleState> = _state.asStateFlow()

    private val notificationCenter = NSNotificationCenter.defaultCenter

    init {
        notificationCenter.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = null,
            usingBlock = {
                _state.value = AppLifecycleState.BACKGROUND
            }
        )
        notificationCenter.addObserverForName(
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null,
            queue = null,
            usingBlock = {
                _state.value = AppLifecycleState.FOREGROUND
            }
        )
    }
}
