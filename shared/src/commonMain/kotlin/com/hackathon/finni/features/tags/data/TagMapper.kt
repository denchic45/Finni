package com.hackathon.finni.features.tags.data

import com.hackathon.finni.api.tag.model.TagResponse
import com.hackathon.finni.data.database.entity.TagEntity

fun TagResponse.toEntity(): TagEntity = TagEntity(
    tagId = id,
    name = name,
    color = color
)

fun TagEntity.toTagResponse(): TagResponse = TagResponse(
    id = tagId,
    name = name,
    color = color
)

fun List<TagEntity>.toTagResponses(): List<TagResponse> = map { it.toTagResponse() }
