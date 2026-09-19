package com.hackathon.finni.core.presentation.validator

/**
 * Условие проверки значения типа [T].
 */
fun interface Condition<T> {
    fun validate(value: T): Boolean

    companion object {
        operator fun <T> invoke(predicate: (value: T) -> Boolean): Condition<T> =
            Condition { value -> predicate(value) }
    }
}

/**
 * Декоратор, который позволяет наблюдать за результатом валидации конкретного условия.
 */
fun <T> Condition<T>.observable(
    onResult: (Boolean) -> Unit
): Condition<T> = Condition { value ->
    validate(value).also { onResult(it) }
}

fun <T> Condition<T>.observable(
    result: ValidationResult
): Condition<T> = observable { result(it) }
