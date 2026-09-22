package com.hackathon.finni.api.tag.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class TagId(val value: Uuid = Uuid.random())

@Serializable
data class TagRequest(
    val name: String,
    val color: String
)

@Serializable
data class TagResponse(
    val id: TagId,
    val name: String,
    val color: String
)
