package com.hackathon.finni.core.ui.navigation.router

import kotlin.reflect.KClass

interface TabNavigator {
    fun navigate(
        tabKlass: KClass<out TopLevelRoute>,
        transformer: List<Destination>.() -> List<Destination>
    ): Boolean
}