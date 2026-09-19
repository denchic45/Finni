package com.hackathon.finni.data.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.hackathon.finni.api.note.model.NoteId
import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.api.tag.model.TagId
import com.hackathon.finni.data.sync.SyncStatus
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    @ColumnInfo(name = "note_id")
    val noteId: NoteId,
    val title: String,
    val content: String,
    @ColumnInfo(name = "project_id")
    val projectId: ProjectId? = null,
    @ColumnInfo(name = "tag_ids")
    val tagIds: List<TagId>,
    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean,
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean,
    @ColumnInfo(name = "reminder")
    val reminder: LocalDateTime?,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus,
    @ColumnInfo(name = "global_index")
    val globalIndex: Int
)
