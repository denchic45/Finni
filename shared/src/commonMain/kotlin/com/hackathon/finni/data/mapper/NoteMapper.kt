package com.hackathon.finni.data.mapper

import androidx.compose.ui.text.input.TextFieldValue
import com.hackathon.finni.api.note.model.NoteResponse
import com.hackathon.finni.data.database.entity.NoteEntity
import com.hackathon.finni.data.sync.SyncStatus
import com.hackathon.finni.features.noteeditor.NoteEditorDraft

fun NoteResponse.toEntity(globalIndex: Int): NoteEntity = NoteEntity(
    noteId = id,
    title = title,
    content = content,
    projectId = projectId,
    tagIds = tagIds,
    isPinned = isPinned,
    isArchived = isArchived,
    isDeleted = isDeleted,
    reminder = reminder,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.SYNCED,
    globalIndex = globalIndex
)

fun NoteEntity.toNoteResponse(): NoteResponse = NoteResponse(
    id = noteId,
    title = title,
    content = content,
    projectId = projectId,
    tagIds = tagIds,
    isPinned = isPinned,
    isArchived = isArchived,
    isDeleted = isDeleted,
    reminder = reminder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun List<NoteEntity>.toNoteResponses(): List<NoteResponse> = map { it.toNoteResponse() }

fun NoteResponse.toDraft(): NoteEditorDraft = NoteEditorDraft(
    title = TextFieldValue(title),
    content = TextFieldValue(content),
    projectId = projectId,
    tagIds = tagIds,
    isPinned = isPinned,
    reminder = reminder
)
