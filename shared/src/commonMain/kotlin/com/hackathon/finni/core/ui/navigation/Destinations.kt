package com.hackathon.finni.core.ui.navigation

import com.hackathon.finni.api.note.model.NoteId
import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.api.tag.model.TagId
import com.hackathon.finni.core.ui.navigation.router.Destination
import com.hackathon.finni.core.ui.navigation.router.Modal
import com.hackathon.finni.core.ui.navigation.router.TopLevelRoute
import kotlinx.serialization.Serializable


@Serializable
data object Splash : TopLevelRoute

@Serializable
data object Home : TopLevelRoute

@Serializable
data object Auth : TopLevelRoute

@Serializable
data object Register : Destination

@Serializable
data class NoteEditor(val noteId: NoteId? = null) : Destination

@Serializable
data class ProjectEditor(val projectId: ProjectId) : Destination

@Serializable
data class TagEditor(val tagId: TagId) : Destination

@Serializable
data class Confirmation(val title: String, val text: String? = null) : Modal

@Serializable
data class OverlayImages(val urls: List<String>, val initialIndex: Int) : Modal

@Serializable
data object GameUiShowcase : Destination


val appTabs = listOf(
    Home
)