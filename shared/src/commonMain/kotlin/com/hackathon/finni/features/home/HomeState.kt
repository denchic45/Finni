package com.hackathon.finni.features.home
import com.hackathon.finni.api.note.model.NoteContext
import com.hackathon.finni.api.note.model.NoteFilter
import com.hackathon.finni.api.note.model.NoteResponse
import com.hackathon.finni.api.tag.model.TagId
import com.hackathon.finni.core.paginator.PageState

sealed interface HomePicker {
    data class Context(val selected: NoteContext) : HomePicker
    data class Tags(val selectedIds: Set<TagId>) : HomePicker
}

data class HomeUiState(
    val filter: NoteFilter = NoteFilter(),
    val notes: List<NoteResponse> = emptyList(),
    val pinnedNotes: List<NoteResponse> = emptyList(),
    val otherNotes: List<NoteResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isSearchExpanded: Boolean = false,
    val activePicker: HomePicker? = null,
    val appendState: PageState = PageState.None,
    val prependState: PageState = PageState.None
)
