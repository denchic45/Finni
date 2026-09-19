package com.hackathon.finni.data.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.hackathon.finni.data.sync.ActionType

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "entity_type")
    val entityType: String,
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    @ColumnInfo(name = "action_type")
    val actionType: ActionType,
    val content: String,
    @ColumnInfo(name = "depends_on_entity_id")
    val dependsOnEntityId: String? = null,
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
