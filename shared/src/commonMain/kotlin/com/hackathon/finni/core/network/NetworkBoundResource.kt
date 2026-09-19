package com.hackathon.finni.core.network

import arrow.core.Either
import arrow.core.Ior
import arrow.core.bothIor
import arrow.core.left
import arrow.core.rightIor
import com.hackathon.finni.api.response.ApiResult
import com.hackathon.finni.core.presentation.error.ApiFailure
import com.hackathon.finni.core.presentation.error.Failure
import com.hackathon.finni.core.presentation.error.asFailure
import com.hackathon.finni.data.RequestResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map


suspend inline fun <T> safeFetch(
    request: suspend () -> ApiResult<T>,
): RequestResult<T> = try {
    request().mapLeft { ApiFailure(it) }
} catch (t: Throwable) {
    t.asFailure().left()
}

inline fun <T> safeObserve(
    crossinline request: () -> Flow<ApiResult<T>>,
): Flow<RequestResult<T>> = try {
    request().map { either -> either.toRequestResult() }
        .catch { t ->
            emit(t.asFailure().left())
        }
} catch (t: Throwable) {
    emptyFlow()
}

fun <T> ApiResult<T>.toRequestResult(): Either<Failure, T> = try {
    mapLeft { ApiFailure(it) }
} catch (t: Throwable) {
    t.asFailure().left()
}

fun <T> observeData(
    query: Flow<T>,
    fetch: suspend () -> ApiResult<*>,
    shouldFetch: (T) -> Boolean = { true },
    waitFetchResult: Boolean = false
): Flow<Ior<Failure, T>> = observeDataAny(
    query = query,
    fetch = { safeFetch { fetch() } },
    shouldFetch = shouldFetch,
    waitFetchResult = waitFetchResult
)

fun <T> observeDataAny(
    query: Flow<T>,
    fetch: suspend () -> Either<Failure, *>,
    shouldFetch: (T) -> Boolean = { true },
    waitFetchResult: Boolean = false
): Flow<Ior<Failure, T>> = flow {

    val first = query.first()

    if (!waitFetchResult) {
        emit(Ior.Right(first))
    }

    if (shouldFetch(first)) {
        val networkResult = fetch()
        networkResult.fold(
            ifLeft = { failure ->
                emitAll(query.map { Ior.Both(failure, it) })
            },
            ifRight = {
                emitAll(query.map { Ior.Right(it) })
            }
        )
    } else {
        emitAll(query.map { Ior.Right(it) })
    }
}.distinctUntilChanged()

suspend fun <T> findData(
    query: suspend () -> T,
    fetch: suspend () -> ApiResult<*>,
    shouldFetch: (T) -> Boolean = { true }
): Ior<Failure, T> = findDataAny(
    query = query,
    fetch = { safeFetch { fetch() } },
    shouldFetch = shouldFetch
)

suspend fun <T> findDataAny(
    query: suspend () -> T,
    fetch: suspend () -> Either<Failure, *>,
    shouldFetch: (T) -> Boolean = { true }
): Ior<Failure, T> {
    val localData = query()
    return if (shouldFetch(localData)) {
        val networkResult = fetch()
        networkResult.fold(
            ifLeft = { failure -> (failure to localData).bothIor() },
            ifRight = { query().rightIor() }
        )
    } else {
        localData.rightIor()
    }
}
