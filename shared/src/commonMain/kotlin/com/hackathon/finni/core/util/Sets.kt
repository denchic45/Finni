package com.hackathon.finni.core.util

fun <T> Set<T>.toggle(item: T): Set<T> {
    return if (contains(item)) this - item else this + item
}

fun <T> MutableSet<T>.toggle(item: T) {
    if (contains(item)) remove(item) else add(item)
}

inline fun <reified T : Enum<T>> Set<T>.clearedIfFull(): Set<T> {
    return if (size == enumValues<T>().size) emptySet() else this
}

inline fun <reified T : Enum<T>> MutableSet<T>.clearIfFull() {
    if (size == enumValues<T>().size) clear()
}