package com.hackathon.finni.core.presentation.handlers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow


sealed interface RefreshTrigger {
    data object Initial : RefreshTrigger
    data object PullToRefresh : RefreshTrigger
    data object Retry : RefreshTrigger
}

interface RefreshHandler {
    val isRefreshing: StateFlow<Boolean>
    val isRetrying: StateFlow<Boolean>

    fun onRefresh()
    fun onRetry()
    fun <T> Flow<T>.bindToRefresh(): Flow<T>
}

class RefreshHandlerImpl : RefreshHandler {
    private val _updateTrigger = Channel<RefreshTrigger>(Channel.CONFLATED)
    private val updateTrigger: Flow<RefreshTrigger> = _updateTrigger.receiveAsFlow()

    override val isRefreshing: StateFlow<Boolean>
        field = MutableStateFlow(false)

    override val isRetrying: StateFlow<Boolean>
        field = MutableStateFlow(false)

    override fun onRefresh() {
        isRefreshing.value = true
        _updateTrigger.trySend(RefreshTrigger.PullToRefresh)
    }

    override fun onRetry() {
        isRetrying.value = true
        _updateTrigger.trySend(RefreshTrigger.Retry)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun <T> Flow<T>.bindToRefresh(): Flow<T> {
        return updateTrigger
            .onStart { emit(RefreshTrigger.Initial) }
            .flatMapLatest { this }
            .onEach { resetStates() }
            .catch { error ->
                resetStates()
                throw error
            }
    }

    private fun resetStates() {
        isRefreshing.value = false
        isRetrying.value = false
    }
}