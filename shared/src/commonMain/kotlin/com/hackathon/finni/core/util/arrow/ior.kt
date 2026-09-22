package com.hackathon.finni.core.util.arrow

import arrow.core.Either
import arrow.core.Ior

inline fun <A, B, R> Ior<A, B>.foldNullable(
    block: (failure: A?, success: B?) -> R
): R = when (this) {
    is Ior.Left -> block(value, null)
    is Ior.Right -> block(null, value)
    is Ior.Both -> block(leftValue, rightValue)
}

inline fun <A, B> Ior<A, B>.onResult(
    block: (failure: A?, success: B?) -> Unit
): Ior<A, B> {
    when (this) {
        is Ior.Left -> block(value, null)
        is Ior.Right -> block(null, value)
        is Ior.Both -> block(leftValue, rightValue)
    }
    return this
}

fun <A, B> Ior<A, B>.toNullablePair(): Pair<A?, B?> = when (this) {
    is Ior.Left -> value to null
    is Ior.Right -> null to value
    is Ior.Both -> leftValue to rightValue
}

fun <A, B> Ior<A, B>.toEitherLeftBiased(): Either<A, B> = fold(
    fa = { Either.Left(it) },
    fb = { Either.Right(it) },
    fab = { a, _ -> Either.Left(a) }
)

fun <A, B> Ior<A, B>.getOrNull(): B? = when (this) {
    is Ior.Left -> null
    is Ior.Right -> value
    is Ior.Both -> rightValue
}