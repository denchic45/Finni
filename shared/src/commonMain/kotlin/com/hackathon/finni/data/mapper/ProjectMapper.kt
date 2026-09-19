package com.hackathon.finni.data.mapper

import com.hackathon.finni.api.project.model.ProjectResponse
import com.hackathon.finni.data.database.entity.ProjectEntity

fun ProjectResponse.toEntity(globalIndex: Int): ProjectEntity = ProjectEntity(
    projectId = id,
    name = name,
    description = description,
    globalIndex = globalIndex
)

fun ProjectEntity.toProjectResponse(): ProjectResponse = ProjectResponse(
    id = projectId,
    name = name,
    description = description
)

fun List<ProjectEntity>.toProjectResponses(): List<ProjectResponse> = map { it.toProjectResponse() }
