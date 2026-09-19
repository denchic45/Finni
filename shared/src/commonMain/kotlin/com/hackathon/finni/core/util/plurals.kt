package com.hackathon.finni.core.util

import kotlin.math.abs

/**
 * Возвращает правильную форму существительного в зависимости от числа (для русского языка).
 *
 * @param one форма для 1, 21, 31... (например, "заметка")
 * @param few форма для 2-4, 22-24... (например, "заметки")
 * @param many форма для 0, 5-19, 100... (например, "заметок")
 */
fun Long.plural(one: String, few: String, many: String): String {
    val n = abs(this) % 100
    val n1 = n % 10
    return when {
        n in 11..19 -> many
        n1 in 2..4 -> few
        n1 == 1L -> one
        else -> many
    }
}

/**
 * Перегрузка для Int
 */
fun Int.plural(one: String, few: String, many: String): String =
    this.toLong().plural(one, few, many)

/**
 * Возвращает строку в формате "число слово" с правильным склонением.
 * Пример: 5.formatPlural("заметка", "заметки", "заметок") -> "5 заметок"
 */
fun Long.formatPlural(one: String, few: String, many: String): String =
    "$this ${plural(one, few, many)}"

fun Int.formatPlural(one: String, few: String, many: String): String =
    "$this ${plural(one, few, many)}"
