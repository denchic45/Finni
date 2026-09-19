package com.hackathon.finni.core.presentation.validator

import kotlin.reflect.KProperty0

/**
 * Базовый интерфейс валидатора.
 */
interface Validator {
    fun validate(): Boolean
}

/**
 * Валидатор конкретного значения.
 */
open class ValueValidator<T>(
    private val provider: () -> T,
    private val conditions: List<Condition<T>>,
    private val operator: Operator<Condition<T>> = Operator.allEach()
) : Validator {
    override fun validate(): Boolean = provider().let { value ->
        conditions.validate(value, operator)
    }
}

/**
 * Композитный валидатор, объединяющий несколько других валидаторов.
 */
open class CompositeValidator(
    private val validators: List<Validator>,
    private val operator: Operator<Validator> = Operator.allEach()
) : Validator {
    override fun validate(): Boolean = validators.validate(operator)

    inline fun onValid(block: () -> Unit) {
        if (validate()) block()
    }
}

/**
 * Декоратор для наблюдения за результатом валидации.
 */
class ObservableValidator(
    private val validator: Validator,
    private val onResult: (Boolean) -> Unit
) : Validator {
    override fun validate(): Boolean = validator.validate().also(onResult)
}

/**
 * DSL точка входа для создания композитного валидатора.
 */
fun compositeValidator(
    operator: Operator<Validator> = Operator.allEach(),
    block: CompositeValidatorBuilder.() -> Unit
): CompositeValidator {
    return CompositeValidatorBuilder(operator).apply(block).build()
}

class CompositeValidatorBuilder(
    private var operator: Operator<Validator> = Operator.allEach()
) {
    private val validators = mutableListOf<Validator>()
    private var onResult: ((Boolean) -> Unit)? = null

    fun operator(operator: Operator<Validator>) {
        this.operator = operator
    }

    fun onResult(block: (Boolean) -> Unit) {
        this.onResult = block
    }

    fun <T> value(
        property: KProperty0<T>,
        operator: Operator<Condition<T>> = Operator.allEach(),
        block: ValueValidatorBuilder<T>.() -> Unit
    ) {
        validators.add(ValueValidatorBuilder(property::get, operator).apply(block).build())
    }

    fun <T> value(
        provider: () -> T,
        operator: Operator<Condition<T>> = Operator.allEach(),
        block: ValueValidatorBuilder<T>.() -> Unit
    ) {
        validators.add(ValueValidatorBuilder(provider, operator).apply(block).build())
    }

    fun build(): CompositeValidator {
        val base = CompositeValidator(validators, operator)
        val resultCallback = onResult ?: return base

        return object : CompositeValidator(validators, operator) {
            override fun validate(): Boolean = base.validate().also { resultCallback(it) }
        }
    }
}

class ValueValidatorBuilder<T>(
    private val provider: () -> T,
    private var operator: Operator<Condition<T>> = Operator.allEach()
) {
    private val conditions = mutableListOf<Condition<T>>()
    private var onResult: ((Boolean) -> Unit)? = null

    fun operator(operator: Operator<Condition<T>>) {
        this.operator = operator
    }

    fun onResult(block: (Boolean) -> Unit) {
        this.onResult = block
    }

    fun check(predicate: (T) -> Boolean) {
        conditions.add(Condition(predicate))
    }

    fun check(predicate: (T) -> Boolean, onResult: (Boolean) -> Unit) {
        conditions.add(Condition(predicate).observable(onResult))
    }

    fun check(predicate: (T) -> Boolean, result: ValidationResult) {
        conditions.add(Condition(predicate).observable(result))
    }

    fun build(): Validator {
        val base = ValueValidator(provider, conditions, operator)
        return if (onResult != null) ObservableValidator(base, onResult!!) else base
    }
}
