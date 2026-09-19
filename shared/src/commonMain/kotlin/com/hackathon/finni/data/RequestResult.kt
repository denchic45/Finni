package com.hackathon.finni.data

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.hackathon.finni.core.presentation.error.Failure
import com.hackathon.finni.core.presentation.error.asThrowable

typealias RequestResult<T> = Either<Failure, T>

typealias EmptyRequestResult = RequestResult<Unit>

fun <T> RequestResult<T>.toEmptyRequestResult(): EmptyRequestResult = fold(
    ifLeft = { it.left() },
    ifRight = { Unit.right() }
)

fun <T> RequestResult<T>.getOrThrow(): T = getOrElse {
    throw it.asThrowable()
}