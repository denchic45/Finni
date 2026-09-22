package com.hackathon.finni.features.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.finni.api.error.ProjectNotFound
import com.hackathon.finni.api.note.model.NoteContext
import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.core.presentation.error.ApiFailure
import com.hackathon.finni.core.presentation.handlers.ErrorHandler
import com.hackathon.finni.core.presentation.handlers.EventHandler
import com.hackathon.finni.core.presentation.handlers.UIEvent
import com.hackathon.finni.core.presentation.model.UiText
import com.hackathon.finni.core.resource.stateInCacheableResource
import com.hackathon.finni.core.ui.navigation.router.ContextSelectedResult
import com.hackathon.finni.core.ui.navigation.router.Router
import com.hackathon.finni.data.repository.ProjectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProjectsUiState(
    val selectedContext: NoteContext,
    val inboxCount: Int = 0,
    val allCount: Int = 0,
    val trashCount: Int = 0,
    val projectCounts: Map<ProjectId, Int> = emptyMap(),
    val isProjectsExpanded: Boolean = true
)

class ContextPickerViewModel(
    val initialContext: NoteContext,
    private val router: Router,
    private val eventHandler: EventHandler,
    private val errorHandler: ErrorHandler,
    private val projectRepository: ProjectRepository
) : ViewModel(), ErrorHandler by errorHandler {

    val projects: StateFlow<com.hackathon.finni.core.resource.CacheableResource<List<com.hackathon.finni.api.project.model.ProjectResponse>>> =
        projectRepository.observeAll().stateInCacheableResource(viewModelScope)

    val state: StateFlow<ProjectsUiState> = combine(
        projectRepository.countInbox(),
        projectRepository.countAll(),
        projectRepository.countTrash(),
        projectRepository.countByProjects()
    ) { inbox, all, trash, projectCounts ->
        ProjectsUiState(
            selectedContext = initialContext,
            inboxCount = inbox,
            allCount = all,
            trashCount = trash,
            projectCounts = projectCounts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProjectsUiState(selectedContext = initialContext)
    )

    fun onContextClick(context: NoteContext) {
        router.sendResult(ContextSelectedResult(context))
    }

    fun onRemoveProjectClick(projectId: ProjectId) {
        launchSafe(
            handler = { uiError ->
                val failure = uiError.origin
                if (failure is ApiFailure && failure.error is ProjectNotFound) {
                    eventHandler.sendEvent(UIEvent.Toast(UiText.Dynamic("Проект не найден")))
                    true
                } else false
            }
        ) {
            val result = projectRepository.remove(projectId)
            if (result is arrow.core.Either.Right) {
                eventHandler.sendEvent(UIEvent.Toast(UiText.Dynamic("Проект успешно удален")))
            }
            result
        }
    }
}
