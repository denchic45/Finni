package com.hackathon.finni.data.repository

import arrow.core.Ior
import com.hackathon.finni.api.project.ProjectApi
import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.api.project.model.ProjectRequest
import com.hackathon.finni.api.project.model.ProjectResponse
import com.hackathon.finni.core.network.observeData
import com.hackathon.finni.core.network.safeFetch
import com.hackathon.finni.core.paginator.Paginator
import com.hackathon.finni.core.presentation.error.Failure
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.database.dao.NoteDao
import com.hackathon.finni.data.database.dao.ProjectDao
import com.hackathon.finni.data.database.withTransaction
import com.hackathon.finni.data.getOrThrow
import com.hackathon.finni.data.mapper.toEntity
import com.hackathon.finni.data.mapper.toProjectResponse
import com.hackathon.finni.data.mapper.toProjectResponses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(
    private val projectApi: ProjectApi,
    private val projectDao: ProjectDao,
    private val noteDao: NoteDao,
    private val database: AppDatabase
) {

    companion object {
        private const val NOTE_PAGE_SIZE = 20
    }

    suspend fun add(request: ProjectRequest) = safeFetch { projectApi.create(request) }
        .onRight {
            database.withTransaction {
                projectDao.incrementGlobalIndexes()
                projectDao.upsert(it.toEntity(0))
            }
        }

    suspend fun update(projectId: ProjectId, request: ProjectRequest) = safeFetch {
        projectApi.update(projectId, request)
    }.onRight {
        database.withTransaction {
            projectDao.update(it.toEntity(getGlobalIndex(projectId)))
        }
    }

    private suspend fun getGlobalIndex(
        projectId: ProjectId
    ): Int = projectDao.getGlobalIndexOrNull(projectId) ?: -1

    suspend fun remove(projectId: ProjectId) = safeFetch {
        projectApi.delete(projectId)
    }.onRight { projectDao.delete(projectId) }

    fun observeById(
        projectId: ProjectId
    ): Flow<Ior<Failure, ProjectResponse>> = observeData(
        query = projectDao.observeById(projectId).map { it.toProjectResponse() },
        fetch = {
            projectApi.getById(projectId)
                .onRight {
                    database.withTransaction {
                        projectDao.upsert(it.toEntity(getGlobalIndex(projectId)))
                    }
                }
        }
    )

    fun observeAll(): Flow<Ior<Failure, List<ProjectResponse>>> = observeData(
        query = projectDao.getAllProjects().map { it.toProjectResponses() },
        fetch = { projectApi.getList(0, 1000) }
    )

    fun countInbox(): Flow<Int> = noteDao.countInbox()
    fun countAll(): Flow<Int> = noteDao.countAll()
    fun countTrash(): Flow<Int> = noteDao.countTrash()
    fun countByProjects(): Flow<Map<ProjectId, Int>> = noteDao.countByProjects()

    fun findPaged() = Paginator(
        pageSize = NOTE_PAGE_SIZE,
        observeLocalPages = { activePages ->
            projectDao.observeByPages(activePages, NOTE_PAGE_SIZE)
        },
        fetchPage = { page ->
            val offset = (page - 1) * NOTE_PAGE_SIZE
            safeFetch { projectApi.getList(offset, NOTE_PAGE_SIZE) }
                .getOrThrow().let { projects ->
                    val entities = projects.mapIndexed { index, project ->
                        project.toEntity(offset + index)
                    }

                    projectDao.replacePage(
                        fromIndex = offset,
                        toIndex = offset + NOTE_PAGE_SIZE - 1,
                        entities = entities
                    )

                    projects.size
                }
        }
    )
}
