package com.hackathon.finni.core.presentation.validator

/**
 * Выполняет предикат для каждого элемента, не прерываясь при нахождении совпадения.
 * Возвращает true, если хотя бы один элемент удовлетворяет условию.
 */
inline fun <T> Iterable<T>.anyEach(predicate: (T) -> Boolean): Boolean {
    return fold(false) { acc, element -> predicate(element) or acc }
}

/**
 * Выполняет предикат для каждого элемента, не прерываясь при нахождении ошибки.
 * Возвращает true, если все элементы удовлетворяют условию.
 */
inline fun <T> Iterable<T>.allEach(predicate: (T) -> Boolean): Boolean {
    return fold(true) { acc, element -> predicate(element) and acc }
}
