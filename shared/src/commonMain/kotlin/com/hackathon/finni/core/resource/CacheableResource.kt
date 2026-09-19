package com.hackathon.finni.core.resource

import arrow.core.Ior
import com.hackathon.finni.core.presentation.error.Failure
import com.hackathon.finni.core.presentation.error.UiError
import com.hackathon.finni.core.presentation.error.toUiError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface CacheableResource<out T> {
    data object Loading : CacheableResource<Nothing>

    data class Newest<out T>(val value: T) : CacheableResource<T>

    // Полноэкранная ошибка (экран пустой)
    data class Failed(val error: UiError) : CacheableResource<Nothing>

    // Фоновая ошибка (данные из кэша на экране, показываем Snackbar/Dialog)
    data class Cached<out T>(val error: UiError, val value: T) : CacheableResource<T>
}

// --- Accessors ---

fun <T> CacheableResource<T>.hasResult(): Boolean = this !is CacheableResource.Loading
fun <T> CacheableResource<T>.isLoading(): Boolean = this is CacheableResource.Loading

fun <T> CacheableResource<T>.getValueOrNull(): T? = when (this) {
    is CacheableResource.Newest -> value
    is CacheableResource.Cached -> value
    else -> null
}

fun <T> CacheableResource<T>.getErrorOrNull(): Any? = when (this) {
    is CacheableResource.Failed -> error
    is CacheableResource.Cached -> error
    else -> null
}

inline fun <T, V> CacheableResource<T>.map(transform: (T) -> V): CacheableResource<V> {
    return when (this) {
        is CacheableResource.Loading -> this
        is CacheableResource.Failed -> CacheableResource.Failed(error)
        is CacheableResource.Newest -> CacheableResource.Newest(transform(value))
        is CacheableResource.Cached -> CacheableResource.Cached(error, transform(value))
    }
}

// --- Mappers for Ior ---

fun <T> Ior<Failure, T>.toCacheableResource(
    localHandler: ((Failure) -> UiError?)? = null
): CacheableResource<T> = when (this) {
    is Ior.Left -> CacheableResource.Failed(value.toUiError(localHandler = localHandler))
    is Ior.Right -> CacheableResource.Newest(value)
    is Ior.Both -> CacheableResource.Cached(leftValue.toUiError(localHandler = localHandler), rightValue)
}

fun <T> Flow<Ior<Failure, T>>.stateInCacheableResource(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.Lazily,
    initialValue: CacheableResource<T> = CacheableResource.Loading,
    localHandler: ((Failure) -> UiError?)? = null
): StateFlow<CacheableResource<T>> = map { it.toCacheableResource(localHandler = localHandler) }
    .stateIn(scope, started, initialValue)
