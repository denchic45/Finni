package com.hackathon.finni.data.sync

import com.hackathon.finni.api.note.NoteApi
import com.hackathon.finni.api.note.model.NoteId
import com.hackathon.finni.api.note.model.NoteRequest
import com.hackathon.finni.api.response.ApiResult
import com.hackathon.finni.api.response.EmptyApiResult
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.database.dao.NoteDao
import com.hackathon.finni.data.database.entity.NoteEntity
import com.hackathon.finni.data.mapper.toEntity
import kotlinx.serialization.KSerializer

class NoteOfflineHandler(
    private val noteApi: NoteApi,
    private val noteDao: NoteDao,
    database: AppDatabase
) : OfflineEntityHandler<NoteId, NoteEntity>(database, database.syncQueueDao()) {

    override val serializer: KSerializer<NoteEntity> = NoteEntity.serializer()
    override val idSerializer: KSerializer<NoteId> = NoteId.serializer()

    override suspend fun localSave(id: NoteId, data: NoteEntity, status: SyncStatus) {
        noteDao.upsert(data.copy(syncStatus = status))
    }

    override suspend fun localDelete(id: NoteId, status: SyncStatus) {
        noteDao.softDelete(id, status)
    }

    override suspend fun remoteCreate(id: NoteId, data: NoteEntity): ApiResult<NoteEntity> {
        val request = NoteRequest(
            title = data.title,
            content = data.content,
            projectId = data.projectId
        )
        return noteApi.create(request).map { response ->
            response.toEntity(data.globalIndex)
        }
    }

    override suspend fun remoteUpdate(id: NoteId, data: NoteEntity): ApiResult<NoteEntity> {
        val request = NoteRequest(
            title = data.title,
            content = data.content,
            projectId = data.projectId
        )
        return noteApi.update(id, request).map { response ->
            response.toEntity(data.globalIndex)
        }
    }

    override suspend fun remoteDelete(id: NoteId): EmptyApiResult {
        return noteApi.delete(id)
    }
}