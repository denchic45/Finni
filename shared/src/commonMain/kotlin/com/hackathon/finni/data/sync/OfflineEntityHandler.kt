package com.hackathon.finni.data.sync

import arrow.core.left
import arrow.core.right
import com.hackathon.finni.api.response.ApiResult
import com.hackathon.finni.api.response.EmptyApiResult
import com.hackathon.finni.core.util.appJson
import com.hackathon.finni.data.RequestResult
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.database.dao.SyncQueueDao
import com.hackathon.finni.data.database.entity.SyncQueueEntity
import com.hackathon.finni.data.database.withTransaction
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.time.Clock

abstract class OfflineEntityHandler<ID : Any, T : Any>(
    private val database: AppDatabase,
    private val queueDao: SyncQueueDao
) {
    /**
     * Коллбэк для мгновенной синхронизации. Устанавливается SyncManager-ом при регистрации.
     */
    internal var onTriggerSync: (suspend (SyncQueueEntity) -> SyncResult)? = null

    val key: String get() = this::class.simpleName ?: error("Handler must have a name")

    abstract val serializer: KSerializer<T>
    abstract val idSerializer: KSerializer<ID>

    val json: Json get() = appJson

    fun serialize(data: T): String = json.encodeToString(serializer, data)
    fun deserialize(content: String): T = json.decodeFromString(serializer, content)

    fun serializeId(id: ID): String = json.encodeToString(idSerializer, id)
    fun deserializeId(content: String): ID = json.decodeFromString(idSerializer, content)

    open val asyncDelayMs: Long get() = 0L

    /**
     * Основной метод для выполнения операций из репозитория.
     */
    protected suspend fun execute(
        id: ID,
        action: ActionType,
        data: T,
        dependsOn: String? = null,
        extraLocalWork: suspend () -> Unit = {}
    ): RequestResult<T> {
        val trigger = onTriggerSync ?: error("Handler $key is not registered in SyncManager!")

        val idString = serializeId(id)
        val now = Clock.System.now().toEpochMilliseconds()
        val content = if (action != ActionType.DELETE) serialize(data) else ""

        val syncStatus = when (action) {
            ActionType.CREATE -> SyncStatus.PENDING_CREATE
            ActionType.UPDATE -> SyncStatus.PENDING_UPDATE
            ActionType.DELETE -> SyncStatus.PENDING_DELETE
        }

        // 1. Атомарная локальная запись
        val queueItem = database.withTransaction {
            extraLocalWork()
            if (action == ActionType.DELETE) localDelete(id, syncStatus)
            else localSave(id, data, syncStatus)

            val queueEntity = SyncQueueEntity(
                entityType = key,
                entityId = idString,
                actionType = action,
                content = content,
                dependsOnEntityId = dependsOn,
                createdAt = now
            )
            val queueId = queueDao.insert(queueEntity)
            queueEntity.copy(id = queueId)
        }

        // 2. Мгновенная попытка синхронизации через менеджер
        return when (val result = trigger(queueItem)) {
            is SyncResult.Synced -> data.right()
            is SyncResult.Queued -> data.right() // Оффлайн-успех
            is SyncResult.Failed -> result.failure.left()
        }
    }

    abstract suspend fun localSave(id: ID, data: T, status: SyncStatus)
    abstract suspend fun localDelete(id: ID, status: SyncStatus)

    abstract suspend fun remoteCreate(id: ID, data: T): ApiResult<T>
    abstract suspend fun remoteUpdate(id: ID, data: T): ApiResult<T>
    abstract suspend fun remoteDelete(id: ID): EmptyApiResult

    // --- Public API for Repository ---

    suspend fun create(
        id: ID,
        entity: T,
        extraLocalWork: suspend () -> Unit = {}
    ) = execute(id, ActionType.CREATE, entity, extraLocalWork = extraLocalWork)

    suspend fun update(
        id: ID,
        entity: T
    ) = execute(id, ActionType.UPDATE, entity)

    suspend fun delete(
        id: ID,
        entity: T
    ) = execute(id, ActionType.DELETE, entity)
}
