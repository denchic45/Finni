package com.hackathon.finni.api.task.model

import com.hackathon.finni.api.project.model.ProjectId
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class TaskId(val value: Uuid = Uuid.random())

@Serializable
data class TaskRequest(
    val title: String,
    val description: String,
    val projectId: ProjectId,
    val isCompleted: Boolean = false
)

@Serializable
data class TaskResponse(
    val id: TaskId,
    val title: String,
    val description: String,
    val projectId: ProjectId,
    val isCompleted: Boolean = false
)
