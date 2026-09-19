package com.hackathon.finni.core.util

fun <T> List<T>.replace(newValue: T, predicate: (T) -> Boolean): List<T> {
    return map {
        if (predicate(it)) newValue else it
    }
}

fun <T> List<T>.replace(index: Int, newValue: T): List<T> {
    return toMutableList().apply { set(index, newValue) }
}

fun <T> List<T>.update(predicate: (T) -> Boolean, transform: (T) -> T): List<T> {
    return map { if (predicate(it)) transform(it) else it }
}

fun <T> List<T>.minusByIndex(index: Int): List<T> {
    return toMutableList().apply { removeAt(index) }
}

fun <T> List<T>.toggle(item: T): List<T> {
    return if (contains(item)) this - item else this + item
}

fun <T> List<T>.move(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex == toIndex) return this
    return toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }
}