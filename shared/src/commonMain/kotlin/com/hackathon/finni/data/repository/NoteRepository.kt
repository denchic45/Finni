package com.hackathon.finni.data.repository

import arrow.core.Ior
import arrow.core.right
import com.hackathon.finni.api.note.NoteApi
import com.hackathon.finni.api.note.model.NoteContext
import com.hackathon.finni.api.note.model.NoteFilter
import com.hackathon.finni.api.note.model.NoteId
import com.hackathon.finni.api.note.model.NoteResponse
import com.hackathon.finni.core.di.ApplicationScope
import com.hackathon.finni.core.network.findData
import com.hackathon.finni.core.network.observeData
import com.hackathon.finni.core.network.safeFetch
import com.hackathon.finni.core.paginator.Paginator
import com.hackathon.finni.core.presentation.error.Failure
import com.hackathon.finni.data.EmptyRequestResult
import com.hackathon.finni.data.RequestResult
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.database.dao.NoteDao
import com.hackathon.finni.data.database.entity.NoteEntity
import com.hackathon.finni.data.database.withTransaction
import com.hackathon.finni.data.getOrThrow
import com.hackathon.finni.data.mapper.toEntity
import com.hackathon.finni.data.mapper.toNoteResponse
import com.hackathon.finni.data.model.CreateNote
import com.hackathon.finni.data.model.UpdateNote
import com.hackathon.finni.data.sync.NoteOfflineHandler
import com.hackathon.finni.data.sync.SyncStatus
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.uuid.Uuid

class NoteRepository(
    private val noteApi: NoteApi,
    private val noteDao: NoteDao,
    private val noteHandler: NoteOfflineHandler,
    private val database: AppDatabase,
    private val appScope: ApplicationScope
) {

    companion object {
        private const val NOTE_PAGE_SIZE = 20
    }

    suspend fun add(createNote: CreateNote): RequestResult<NoteResponse> {
        val id = NoteId(Uuid.random())
        val now = Clock.System.now()
        val entity = NoteEntity(
            noteId = id,
            title = createNote.title,
            content = createNote.content,
            projectId = createNote.projectId,
            tagIds = createNote.tagIds,
            isPinned = createNote.isPinned,
            isArchived = false,
            isDeleted = false,
            reminder = createNote.reminder,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_CREATE,
            globalIndex = 0
        )

        return noteHandler.create(
            id = id,
            entity = entity,
            extraLocalWork = { noteDao.incrementGlobalIndexes() }
        ).map { it.toNoteResponse() }
    }

    suspend fun update(noteId: NoteId, updateNote: UpdateNote): RequestResult<NoteResponse> {
        val existing = noteDao.getById(noteId) ?: throw IllegalStateException("Note not found")
        val updatedEntity = existing.copy(
            title = updateNote.title,
            content = updateNote.content,
            projectId = updateNote.projectId,
            tagIds = updateNote.tagIds,
            isPinned = updateNote.isPinned,
            reminder = updateNote.reminder,
            updatedAt = Clock.System.now()
        )

        return noteHandler.update(noteId, updatedEntity).map { it.toNoteResponse() }
    }

    fun addAsync(createNote: CreateNote) {
        appScope.launch { add(createNote) }
    }

    fun updateAsync(noteId: NoteId, updateNote: UpdateNote) {
        appScope.launch { update(noteId, updateNote) }
    }

    suspend fun remove(noteId: NoteId): EmptyRequestResult {
        val existing = noteDao.getById(noteId) ?: return Unit.right()

        return noteHandler.delete(noteId, existing).map { }
    }

    private suspend fun getGlobalIndex(
        noteId: NoteId
    ): Int = noteDao.getGlobalIndexOrNull(noteId) ?: -1

    suspend fun findById(noteId: NoteId): Ior<Failure, NoteResponse?> = findData(
        query = { noteDao.getById(noteId)?.toNoteResponse() },
        fetch = { noteApi.getById(noteId) }
    )

    fun observeById(
        noteId: NoteId
    ) = observeData(
        query = noteDao.observeById(noteId),
        fetch = {
            noteApi.getById(noteId)
                .onRight { noteResponse: NoteResponse ->
                    database.withTransaction {
                        noteDao.upsert(noteResponse.toEntity(getGlobalIndex(noteId)))
                    }
                }
        }
    )

    fun findPaged(filter: NoteFilter = NoteFilter()): Paginator<NoteResponse> = Paginator(
        pageSize = NOTE_PAGE_SIZE,
        observeLocalPages = { activePages ->
            val context = filter.context
            noteDao.observeByPages(
                pages = activePages,
                pageSize = NOTE_PAGE_SIZE,
                searchQuery = filter.searchQuery.takeIf { it.isNotBlank() },
                projectId = (context as? NoteContext.Project)?.id,
                isArchived = if (context is NoteContext.Archive) true else if (context is NoteContext.Trash) null else false,
                isDeleted = context is NoteContext.Trash,
                hasReminder = filter.hasReminderOnly,
                sortOrder = filter.sortOrder.name
            ).map { pageMap ->
                pageMap.mapValues { entry ->
                    entry.value.map { it.toNoteResponse() }
                }
            }
        },
        fetchPage = { page ->
            val offset = (page - 1) * NOTE_PAGE_SIZE
            safeFetch { noteApi.getList(offset, NOTE_PAGE_SIZE, filter) }
                .getOrThrow().let { notes: List<NoteResponse> ->
                    val entities = notes.mapIndexed { index, note ->
                        note.toEntity(offset + index)
                    }

                    noteDao.replacePage(
                        fromIndex = offset,
                        toIndex = offset + NOTE_PAGE_SIZE - 1,
                        entities = entities
                    )

                    notes.size
                }
        }
    )
}
