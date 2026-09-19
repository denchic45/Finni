package com.hackathon.finni.features.noteeditor

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.finni.api.note.model.NoteId
import com.hackathon.finni.api.note.model.NoteResponse
import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.core.presentation.handlers.ErrorHandler
import com.hackathon.finni.core.presentation.handlers.EventHandler
import com.hackathon.finni.core.presentation.handlers.LoadingHandler
import com.hackathon.finni.core.presentation.handlers.LoadingMode
import com.hackathon.finni.core.presentation.handlers.UIEvent
import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.core.ui.navigation.router.pop
import com.hackathon.finni.core.util.arrow.toEitherLeftBiased
import com.hackathon.finni.core.util.createLogger
import com.hackathon.finni.data.mapper.toDraft
import com.hackathon.finni.data.model.CreateNote
import com.hackathon.finni.data.model.UpdateNote
import com.hackathon.finni.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class NoteEditorViewModel(
    private val noteId: NoteId?,
    private val router: Router,
    private val eventHandler: EventHandler,
    private val errorHandler: ErrorHandler,
    private val loadingHandler: LoadingHandler,
    private val noteRepository: NoteRepository
) : ViewModel(), ErrorHandler by errorHandler, LoadingHandler by loadingHandler {
    private val logger = createLogger()
    private val _originalNote = MutableStateFlow<NoteResponse?>(null)
    private val _draft = MutableStateFlow(NoteEditorDraft())
    private val _isLoading = MutableStateFlow(noteId != null)

    private val historyManager = NoteHistoryManager(viewModelScope)

    val uiState: StateFlow<NoteEditorUiState> = combine(
        _isLoading,
        _originalNote,
        _draft
    ) { isLoading, original, draft ->
        val hasChanges = if (original == null) {
            draft.title.text.isNotBlank() || draft.content.text.isNotBlank()
        } else {
            original.toDraft() != draft
        }

        NoteEditorUiState(
            draft = draft,
            isNewNote = noteId == null,
            hasUnsavedChanges = hasChanges,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NoteEditorUiState(isLoading = noteId != null)
    )

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    private fun loadNote(id: NoteId) {
        launchSafe(loadingMode = LoadingMode.None) {
            val result = noteRepository.findById(id)

            result.getOrNull()?.let { note ->
                _originalNote.value = note
                _draft.value = note.toDraft()
            }

            _isLoading.value = false

            result.toEitherLeftBiased()
        }
    }

    fun onTitleChange(newValue: TextFieldValue) {
        _draft.update { it.copy(title = newValue) }
        historyManager.recordChange(newValue, _draft.value.content)
    }

    fun onContentChange(newValue: TextFieldValue) {
        _draft.update { it.copy(content = newValue) }
        historyManager.recordChange(_draft.value.title, newValue)
    }

    fun onProjectSelect(projectId: ProjectId?) {
        _draft.update { it.copy(projectId = projectId) }
    }

    fun onTogglePin() {
        _draft.update { it.copy(isPinned = !it.isPinned) }
    }

    fun onToggleReminder() {
        TODO("Показывать диалог с выбранной или текущей датой и временем")
//        _draft.update { it.copy(reminder = !it.reminder) }
    }

    fun onUndo() {
        historyManager.undo(_draft.value.title, _draft.value.content)?.let { snapshot ->
            _draft.update { it.copy(title = snapshot.title, content = snapshot.content) }
        }
    }

    fun onRedo() {
        historyManager.redo(_draft.value.title, _draft.value.content)?.let { snapshot ->
            _draft.update { it.copy(title = snapshot.title, content = snapshot.content) }
        }
    }

    fun onFocusLost() {
        historyManager.recordChange(_draft.value.title, _draft.value.content, force = true)
    }

    fun onDiscardChanges() {
        _draft.value = _originalNote.value?.toDraft() ?: NoteEditorDraft()
    }

    fun onSaveClick(onSuccess: () -> Unit) {
        val currentUiState = uiState.value
        if (!currentUiState.hasUnsavedChanges) return

        val draft = _draft.value
        val projectId = draft.projectId ?: return

        launchSafe(loadingMode = LoadingMode.Delayed()) {
            val result = if (noteId == null) {
                noteRepository.add(
                    CreateNote(
                        title = draft.title.text,
                        content = draft.content.text,
                        projectId = projectId,
                        tagIds = draft.tagIds,
                        isPinned = draft.isPinned,
                        reminder = draft.reminder
                    )
                )
            } else {
                noteRepository.update(
                    noteId,
                    UpdateNote(
                        title = draft.title.text,
                        content = draft.content.text,
                        projectId = projectId,
                        tagIds = draft.tagIds,
                        isPinned = draft.isPinned,
                        reminder = draft.reminder
                    )
                )
            }

            result.onRight {
                eventHandler.sendEvent(UIEvent.Toast(UiText.Dynamic("Заметка сохранена")))
                onSuccess()
            }
        }
    }

    fun onBack() {
        router.pop()
    }

    override fun onCleared() {
        super.onCleared()
        val currentUiState = uiState.value
        if (!currentUiState.hasUnsavedChanges) return

        val draft = _draft.value
        logger.i { "Saving note" }
        if (noteId == null) {
            // Создаем только если есть текст
            if (draft.title.text.isNotBlank() || draft.content.text.isNotBlank()) {
                logger.i { "add note" }
                noteRepository.addAsync(
                    CreateNote(
                        title = draft.title.text,
                        content = draft.content.text,
                        projectId = draft.projectId ?: return,
                        tagIds = draft.tagIds,
                        isPinned = draft.isPinned,
                        reminder = draft.reminder
                    )
                )
            }
        } else {
            logger.i { "update note" }
            noteRepository.updateAsync(
                noteId,
                UpdateNote(
                    title = draft.title.text,
                    content = draft.content.text,
                    projectId = draft.projectId ?: return,
                    tagIds = draft.tagIds,
                    isPinned = draft.isPinned,
                    reminder = draft.reminder
                )
            )
        }
    }
}
