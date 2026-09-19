package com.hackathon.finni.core.ui.navigation.router

import com.hackathon.finni.api.tag.model.TagId
import com.hackathon.finni.api.note.model.NoteContext
import kotlinx.serialization.Serializable

sealed interface NavigationResult

@Serializable
data class Confirmed(val confirmed: Boolean) : NavigationResult

@Serializable
data class LatestPhotoIndex(val index: Int) : NavigationResult

@Serializable
data class ContextSelectedResult(val context: NoteContext) : NavigationResult

@Serializable
data class TagsSelectedResult(val tagIds: Set<TagId>) : NavigationResult
