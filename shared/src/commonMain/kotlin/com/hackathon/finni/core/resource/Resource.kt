package com.hackathon.finni.core.resource

import arrow.core.Either
import com.hackathon.finni.core.presentation.error.Failure
import com.hackathon.finni.core.presentation.error.UiError
import com.hackathon.finni.core.presentation.error.toUiError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<out T>(val value: T) : Resource<T>
    data class Failed(val error: UiError) : Resource<Nothing>
}

// --- Accessors ---

fun <T> Resource<T>.hasResult(): Boolean = this !is Resource.Loading
fun <T> Resource<T>.isLoading(): Boolean = this is Resource.Loading

fun <T> Resource<T>.getValueOrNull(): T? = (this as? Resource.Success)?.value
fun <T> Resource<T>.errorOrNull(): UiError? = (this as? Resource.Failed)?.error

inline fun <T, V> Resource<T>.map(transform: (T) -> V): Resource<V> {
    return when (this) {
        is Resource.Loading -> this
        is Resource.Failed -> Resource.Failed(error)
        is Resource.Success -> Resource.Success(transform(value))
    }
}

// --- Mappers for Either ---

fun <T> Either<Failure, T>.toResource(
    onRetry: (() -> Unit)? = null,
    localHandler: ((Failure) -> UiError?)? = null
): Resource<T> = fold(
    ifLeft = { failure ->
        Resource.Failed(
            failure.toUiError(
//            onAction = onRetry,
                localHandler = localHandler
            )
        )
    },
    ifRight = { value ->
        Resource.Success(value)
    }
)

fun <T> Flow<Either<Failure, T>>.stateInResource(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.Lazily,
    initialValue: Resource<T> = Resource.Loading,
    onRetry: (() -> Unit)? = null,
    localHandler: ((Failure) -> UiError?)? = null
): StateFlow<Resource<T>> = map { it.toResource(onRetry = onRetry, localHandler = localHandler) }
    .stateIn(scope, started, initialValue)