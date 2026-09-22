package com.hackathon.finni.data.sync

import com.hackathon.finni.api.error.UnknownError
import com.hackathon.finni.core.di.ApplicationScope
import com.hackathon.finni.core.lifecycle.AppLifecycleObserver
import com.hackathon.finni.core.lifecycle.AppLifecycleState
import com.hackathon.finni.core.network.NetworkObserver
import com.hackathon.finni.core.network.NetworkStatus
import com.hackathon.finni.core.network.safeFetch
import com.hackathon.finni.core.presentation.error.ApiFailure
import com.hackathon.finni.core.presentation.error.Failure
import com.hackathon.finni.core.presentation.error.NoConnection
import com.hackathon.finni.core.presentation.error.Timeout
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.database.dao.SyncQueueDao
import com.hackathon.finni.data.database.entity.SyncQueueEntity
import com.hackathon.finni.data.database.withTransaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.milliseconds

class SyncManager(
    private val handlers: Map<String, OfflineEntityHandler<*, *>>,
    private val queueDao: SyncQueueDao,
    private val database: AppDatabase,
    private val networkObserver: NetworkObserver,
    private val lifecycleObserver: AppLifecycleObserver,
    private val scope: ApplicationScope
) {
    private val mutex = Mutex()
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        // Регистрация коллбэков в хендлерах
        handlers.values.forEach { handler ->
            handler.onTriggerSync = { item -> processItem(item) }
        }

        // Автоматическая синхронизация при смене условий
        combine(
            networkObserver.status,
            lifecycleObserver.state
        ) { net, life -> net == NetworkStatus.CONNECTED && life == AppLifecycleState.FOREGROUND }
            .onEach { isReady -> if (isReady) sync() }
            .launchIn(scope)
    }

    /**
     * Запускает цикл обработки всей очереди.
     * @param force Если true, игнорирует временные ошибки и пытается выполнить всё немедленно.
     */
    fun sync(force: Boolean = false) {
        scope.launch {
            mutex.withLock {
                if (_isSyncing.value) return@launch
                _isSyncing.value = true
                try {
                    val items = queueDao.getAll()
                    for (item in items) {
                        val result = processItem(item)
                        if (result is SyncResult.Queued && !force) {
                            // Если не форсируем и возникла проблема с сетью — прерываем цикл
                            break
                        }
                    }
                } finally {
                    _isSyncing.value = false
                }
            }
        }
    }

    /**
     * Обрабатывает одну задачу из очереди.
     */
    private suspend fun processItem(item: SyncQueueEntity): SyncResult {
        val handler = handlers[item.entityType] ?: return SyncResult.Failed(
            ApiFailure(UnknownError(0, "Handler not found"))
        )

        return executeTyped(handler as OfflineEntityHandler<Any, Any>, item)
    }

    private suspend fun <ID : Any, T : Any> executeTyped(
        handler: OfflineEntityHandler<ID, T>,
        item: SyncQueueEntity
    ): SyncResult {
        val typedId = handler.deserializeId(item.entityId)

        val networkResult: com.hackathon.finni.data.RequestResult<Any?> = safeFetch {
            when (item.actionType) {
                ActionType.CREATE -> {
                    val data = handler.deserialize(item.content)
                    handler.remoteCreate(typedId, data)
                }
                ActionType.UPDATE -> {
                    val data = handler.deserialize(item.content)
                    handler.remoteUpdate(typedId, data)
                }
                ActionType.DELETE -> {
                    handler.remoteDelete(typedId).map { Unit }
                }
            }
        }

        val result: SyncResult = when (networkResult) {
            is arrow.core.Either.Left -> {
                val failure = networkResult.value
                if (isTransientError(failure)) {
                    queueDao.incrementRetry(item.id)
                    SyncResult.Queued(failure)
                } else {
                    database.withTransaction {
                        queueDao.deleteById(item.id)
                        if (item.actionType != ActionType.DELETE) {
                            val data = handler.deserialize(item.content)
                            handler.localSave(typedId, data, SyncStatus.SYNC_FAILED)
                        }
                    }
                    SyncResult.Failed(failure)
                }
            }

            is arrow.core.Either.Right -> {
                val response = networkResult.value
                database.withTransaction {
                    queueDao.deleteById(item.id)
                    if (item.actionType != ActionType.DELETE) {
                        @Suppress("UNCHECKED_CAST")
                        handler.localSave(typedId, response as T, SyncStatus.SYNCED)
                    }
                }
                SyncResult.Synced
            }
        }
        delay(handler.asyncDelayMs.milliseconds)
        return result
    }

    private fun isTransientError(failure: Failure): Boolean {
        return failure is NoConnection ||
                failure is Timeout ||
                (failure is ApiFailure && failure.httpStatusCode.value >= 500)
    }
}
