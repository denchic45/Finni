package com.hackathon.finni.core.ui.navigation.router

import kotlin.reflect.KClass


private fun List<Destination>.transformBringToFront(destination: Destination): List<Destination> {
    return filterNot { it::class == destination::class } + destination
}

private fun List<Destination>.transformPush(destination: Destination) = this + destination

private fun List<Destination>.transformPushNew(destination: Destination): List<Destination> {
    return if (lastOrNull() == destination) this else this + destination
}

private fun List<Destination>.transformPop() = dropLast(1)

private fun List<Destination>.popWhile(predicate: (Destination) -> Boolean): List<Destination> {
    return dropLastWhile(predicate)
}

private fun List<Destination>.transformPopTo(index: Int): List<Destination> {
    require(index >= 0) { "Index must not negative, but was $index" }
    return take(index + 1)
}

private fun List<Destination>.transformReplaceCurrent(destination: Destination): List<Destination> {
    return dropLast(1) + destination
}

private fun transformReplaceAll(vararg destinations: Destination): List<Destination> {
    return destinations.toList()
}

fun Navigator.bringToFront(c: Destination) = navigate { transformBringToFront(c) }

fun Navigator.push(c: Destination) = navigate { transformPush(c) }

fun Navigator.pushNew(c: Destination) = navigate { transformPushNew(c) }

fun Navigator.pop() = navigate { transformPop() }

fun Navigator.popTo(index: Int) = navigate { transformPopTo(index) }

fun Navigator.popWhile(predicate: (Destination) -> Boolean) = navigate { dropLastWhile(predicate) }

fun Navigator.replaceCurrent(c: Destination) = navigate { transformReplaceCurrent(c) }

fun Navigator.replaceAll(vararg c: Destination) = navigate { c.toList() }

// --- Расширения для TabNavigator (Конкретная вкладка) ---

fun TabNavigator.bringToFront(tab: KClass<out TopLevelRoute>, c: Destination): Boolean {
    return navigate(tab) { transformBringToFront(c) }
}

fun TabNavigator.push(tab: KClass<out TopLevelRoute>, c: Destination): Boolean {
    return navigate(tab) { transformPush(c) }
}

fun TabNavigator.pushNew(tab: KClass<out TopLevelRoute>, c: Destination): Boolean {
    return navigate(tab) { transformPushNew(c) }
}

fun TabNavigator.pop(tab: KClass<out TopLevelRoute>): Boolean {
    return navigate(tab) { transformPop() }
}

fun TabNavigator.popTo(tab: KClass<out TopLevelRoute>, index: Int): Boolean {
    return navigate(tab) { transformPopTo(index) }
}

fun TabNavigator.popWhile(tab: KClass<out TopLevelRoute>, predicate: (Destination) -> Boolean): Boolean {
    return navigate(tab) { dropLastWhile(predicate) }
}

fun TabNavigator.replaceCurrent(tab: KClass<out TopLevelRoute>, c: Destination): Boolean {
    return navigate(tab) { transformReplaceCurrent(c) }
}

fun TabNavigator.replaceAll(tab: KClass<out TopLevelRoute>, vararg c: Destination): Boolean {
    return navigate(tab) { c.toList() }
}