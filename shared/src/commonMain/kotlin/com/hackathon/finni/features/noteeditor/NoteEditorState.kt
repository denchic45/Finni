package com.hackathon.finni.features.noteeditor

import androidx.compose.ui.text.input.TextFieldValue
import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.api.tag.model.TagId
import kotlinx.datetime.LocalDateTime

data class NoteEditorUiState(
    val draft: NoteEditorDraft = NoteEditorDraft(),
    val isNewNote: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val isLoading: Boolean = false
)

data class NoteEditorDraft(
    val title: TextFieldValue = TextFieldValue(""),
    val content: TextFieldValue = TextFieldValue(""),
    val projectId: ProjectId? = null,
    val tagIds: List<TagId> = emptyList(),
    val isPinned: Boolean = false,
    val reminder: LocalDateTime? = null
)
