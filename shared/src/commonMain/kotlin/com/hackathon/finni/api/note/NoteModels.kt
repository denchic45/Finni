@file:kotlinx.serialization.UseSerializers(com.hackathon.finni.core.util.InstantSerializer::class)

package com.hackathon.finni.api.note.model

import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.api.tag.model.TagId
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class NoteId(val value: Uuid = Uuid.random())

@Serializable
data class NoteResponse(
    val id: NoteId,
    val title: String,
    val content: String,
    val projectId: ProjectId? = null,
    val tagIds: List<TagId> = emptyList(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val reminder: LocalDateTime? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Serializable
data class NoteRequest(
    val title: String,
    val content: String,
    val projectId: ProjectId? = null,
    val tagIds: List<TagId> = emptyList(),
    val isPinned: Boolean = false,
    val reminder: LocalDateTime? = null
)

@Serializable
sealed interface NoteContext {
    @Serializable
    data object All : NoteContext

    @Serializable
    data object Inbox : NoteContext

    @Serializable
    data class Project(val id: ProjectId, val name: String = "") : NoteContext

    @Serializable
    data object Archive : NoteContext

    @Serializable
    data object Trash : NoteContext
}

@Serializable
enum class SortOrder {
    CREATED_DESC,
    CREATED_ASC,
    UPDATED_DESC
}

@Serializable
data class NoteFilter(
    val context: NoteContext = NoteContext.All,
    val hasReminderOnly: Boolean = false,
    val selectedTagIds: Set<TagId> = emptySet(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.CREATED_DESC
) {
    val hasActiveFilters: Boolean
        get() = context != NoteContext.All || hasReminderOnly || selectedTagIds.isNotEmpty() || searchQuery.isNotEmpty()
}
