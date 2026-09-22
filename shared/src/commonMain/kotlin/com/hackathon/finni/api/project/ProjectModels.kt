package com.hackathon.finni.api.project.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class ProjectId(val value: Uuid = Uuid.random())

@Serializable
data class ProjectRequest(
    val name: String,
    val description: String
)

@Serializable
data class ProjectResponse(
    val id: ProjectId,
    val name: String,
    val description: String
)
