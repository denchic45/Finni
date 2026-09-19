package com.hackathon.finni.data.model

import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.api.tag.model.TagId
import kotlinx.datetime.LocalDateTime

data class CreateNote(
    val title: String,
    val content: String,
    val projectId: ProjectId? = null,
    val tagIds: List<TagId> = emptyList(),
    val isPinned: Boolean = false,
    val reminder: LocalDateTime? = null
)

data class UpdateNote(
    val title: String,
    val content: String,
    val projectId: ProjectId? = null,
    val tagIds: List<TagId>,
    val isPinned: Boolean,
    val reminder: LocalDateTime? = null
)
