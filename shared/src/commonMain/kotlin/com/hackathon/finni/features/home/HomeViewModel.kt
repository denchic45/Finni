package com.hackathon.finni.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.finni.api.note.model.NoteId
import com.hackathon.finni.api.note.model.NoteResponse
import com.hackathon.finni.core.paginator.PageState
import com.hackathon.finni.core.paginator.Paginator
import com.hackathon.finni.core.paginator.PaginatorUIState
import com.hackathon.finni.core.presentation.handlers.ErrorHandler
import com.hackathon.finni.core.presentation.handlers.RefreshHandler
import com.hackathon.finni.core.ui.navigation.GameUiShowcase
import com.hackathon.finni.core.ui.navigation.NoteEditor
import com.hackathon.finni.core.ui.navigation.router.ContextSelectedResult
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.router.TagsSelectedResult
import com.hackathon.finni.core.ui.navigation.router.push
import com.hackathon.finni.data.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val router: Router,
    private val errorHandler: ErrorHandler,
    private val refreshHandler: RefreshHandler,
    private val noteRepository: NoteRepository
) : ViewModel(), ErrorHandler by errorHandler, RefreshHandler by refreshHandler {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _paginator = MutableStateFlow(noteRepository.findPaged(_uiState.value.filter))
    val paginator: StateFlow<Paginator<NoteResponse>> = _paginator.asStateFlow()

    init {
        _paginator
            .flatMapLatest { it.uiState }
            .bindToRefresh()
            .onEach { pState ->
                val allNotes = (pState as? PaginatorUIState.Content<NoteResponse>)?.items ?: emptyList()
                val isPaginatorLoading = pState is PaginatorUIState.Loading ||
                        (pState as? PaginatorUIState.Content<*>)?.appendState is PageState.Loading

                val append = (pState as? PaginatorUIState.Content<*>)?.appendState ?: PageState.None
                val prepend = (pState as? PaginatorUIState.Content<*>)?.prependState ?: PageState.None

                _uiState.update { current ->
                    current.copy(
                        notes = allNotes,
                        pinnedNotes = allNotes.filter { it.isPinned },
                        otherNotes = allNotes.filter { !it.isPinned },
                        isLoading = isPaginatorLoading,
                        appendState = append,
                        prependState = prepend
                    )
                }
            }
            .launchIn(viewModelScope)

        loadNotes(initial = true)
    }

    // --- Actions ---

    override fun onRefresh() {
        refreshHandler.onRefresh()
        loadNotes(initial = true)
    }

    override fun onRetry() {
        refreshHandler.onRetry()
        loadNotes(initial = true)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(filter = it.filter.copy(searchQuery = query)) }
        loadNotes(initial = true)
    }

    fun onSearchExpandToggle(expanded: Boolean) {
        _uiState.update { it.copy(isSearchExpanded = expanded) }
        if (!expanded && _uiState.value.filter.searchQuery.isNotEmpty()) {
            onSearchQueryChange("")
        }
    }

    fun onToggleReminderOnly() {
        _uiState.update { it.copy(filter = it.filter.copy(hasReminderOnly = !it.filter.hasReminderOnly)) }
        loadNotes(initial = true)
    }

    fun onLoadNextPage() {
        loadNotes(initial = false)
    }

    fun onNoteClick(noteId: NoteId) {
        router.push(NoteEditor(noteId))
    }

    fun onCreateNoteClick() {
        router.push(NoteEditor(noteId = null))
    }

    fun onOpenGameUiShowcase() {
        router.push(GameUiShowcase)
    }

    // --- Navigation (Pickers) ---

    fun onSelectContextClick() {
        _uiState.update { it.copy(activePicker = HomePicker.Context(it.filter.context)) }
        viewModelScope.launch {
            val result = router.receiveResult<ContextSelectedResult>()
            _uiState.update { it.copy(activePicker = null) }
            if (result != null) {
                _uiState.update { it.copy(filter = it.filter.copy(context = result.context)) }
                loadNotes(initial = true)
            }
        }
    }

    fun onSelectTagsClick() {
        _uiState.update { it.copy(activePicker = HomePicker.Tags(it.filter.selectedTagIds)) }
        viewModelScope.launch {
            val result = router.receiveResult<TagsSelectedResult>()
            _uiState.update { it.copy(activePicker = null) }
            if (result != null) {
                _uiState.update { it.copy(filter = it.filter.copy(selectedTagIds = result.tagIds)) }
                loadNotes(initial = true)
            }
        }
    }

    fun onDismissPicker() {
        _uiState.update { it.copy(activePicker = null) }
    }

    private fun loadNotes(initial: Boolean = false) {
        val p = if (initial) {
            noteRepository.findPaged(_uiState.value.filter).also { _paginator.value = it }
        } else {
            _paginator.value
        }

        viewModelScope.launch {
            if (initial) p.restart() else p.loadNext()
        }
    }
}
