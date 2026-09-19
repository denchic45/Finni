package com.hackathon.finni.core.presentation.validator

/**
 * Оператор объединения результатов валидации.
 * Определяет, как интерпретировать список условий (все должны быть верны, или хотя бы одно).
 */
fun interface Operator<T> {
    operator fun invoke(items: List<T>, predicate: (T) -> Boolean): Boolean

    companion object {
        /** Short-circuit: возвращает true, если все верны. Прерывается на первой ошибке. */
        fun <T> all(): Operator<T> = Operator { list, predicate -> list.all(predicate) }

        /** Short-circuit: возвращает true, если хотя бы один верен. Прерывается на первом успехе. */
        fun <T> any(): Operator<T> = Operator { list, predicate -> list.any(predicate) }

        /** Full-execution: выполняет все проверки. Возвращает true, если все верны. */
        fun <T> allEach(): Operator<T> = Operator { items, predicate -> items.allEach(predicate) }

        /** Full-execution: выполняет все проверки. Возвращает true, если хотя бы один верен. */
        fun <T> anyEach(): Operator<T> = Operator { items, predicate -> items.anyEach(predicate) }
    }
}

/**
 * Расширение для запуска валидации списка условий для конкретного значения.
 */
fun <T> List<Condition<T>>.validate(value: T, operator: Operator<Condition<T>>): Boolean {
    return operator(this) { it.validate(value) }
}

/**
 * Расширение для запуска валидации списка валидаторов.
 */
fun List<Validator>.validate(operator: Operator<Validator>): Boolean {
    return operator(this) { it.validate() }
}
