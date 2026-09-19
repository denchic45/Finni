package com.hackathon.finni.core.ui.navigation.router

import kotlin.reflect.KClass

data class NavigationState<T>(
    val tabStacks: Map<KClass<out TopLevelRoute>, List<T>>,
    val currentTab: KClass<out TopLevelRoute>
) {
    val flattenedBackStack: List<T> get() = tabStacks.flatMap { it.value }
}