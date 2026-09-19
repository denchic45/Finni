package com.hackathon.finni.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.hackathon.finni.data.sync.ActionType
import com.hackathon.finni.data.database.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    @Insert
    suspend fun insert(entity: SyncQueueEntity): Long

    @Update
    suspend fun update(entity: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sync_queue WHERE entity_type = :type AND entity_id = :id")
    suspend fun deleteByEntity(type: String, id: String)

    @Query("SELECT * FROM sync_queue WHERE entity_type = :type AND entity_id = :id LIMIT 1")
    suspend fun getPendingAction(type: String, id: String): SyncQueueEntity?

    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC")
    fun observeAll(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC")
    suspend fun getAll(): List<SyncQueueEntity>

    @Transaction
    suspend fun enqueueOrCollapse(
        type: String,
        id: String,
        newAction: ActionType,
        newContent: String,
        dependsOn: String? = null,
        now: Long
    ) {
        val existing = getPendingAction(type, id)

        if (existing == null) {
            insert(
                SyncQueueEntity(
                    entityType = type,
                    entityId = id,
                    actionType = newAction,
                    content = newContent,
                    dependsOnEntityId = dependsOn,
                    createdAt = now
                )
            )
            return
        }

        // Перезапись при одинаковом типе операции
        if (existing.actionType == newAction) {
            update(existing.copy(content = newContent, createdAt = now))
            return
        }

        when (existing.actionType to newAction) {
            ActionType.CREATE to ActionType.UPDATE -> {
                update(existing.copy(content = newContent, createdAt = now))
            }
            ActionType.CREATE to ActionType.DELETE -> {
                deleteById(existing.id)
            }
            ActionType.UPDATE to ActionType.DELETE -> {
                update(existing.copy(actionType = ActionType.DELETE, content = newContent, createdAt = now))
            }
            else -> {
                update(existing.copy(actionType = newAction, content = newContent, createdAt = now))
            }
        }
    }

    @Query("UPDATE sync_queue SET retry_count = retry_count + 1 WHERE id = :id")
    suspend fun incrementRetry(id: Long)
}
