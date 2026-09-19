package com.hackathon.finni.data.repository

import com.hackathon.finni.api.tag.TagApi
import com.hackathon.finni.api.tag.model.TagId
import com.hackathon.finni.api.tag.model.TagRequest
import com.hackathon.finni.core.network.observeData
import com.hackathon.finni.core.network.safeFetch
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.database.dao.TagDao
import com.hackathon.finni.data.database.withTransaction
import com.hackathon.finni.features.tags.data.toEntity

class TagRepository(
    private val tagApi: TagApi,
    private val tagDao: TagDao,
    private val database: AppDatabase
) {

    suspend fun add(request: TagRequest) = safeFetch { tagApi.create(request) }
        .onRight { tagDao.upsert(it.toEntity()) }

    suspend fun update(tagId: TagId, request: TagRequest) = safeFetch {
        tagApi.update(tagId, request)
    }.onRight { tagDao.update(it.toEntity()) }

    suspend fun remove(tagId: TagId) = safeFetch {
        tagApi.delete(tagId)
    }.onRight { tagDao.delete(tagId) }

    fun observeById(
        tagId: TagId
    ) = observeData(
        query = tagDao.observeById(tagId),
        fetch = {
            tagApi.getById(tagId)
                .onRight { tagDao.upsert(it.toEntity()) }
        }
    )

    fun findAll() = observeData(
        query = tagDao.getAll(),
        fetch = {
            tagApi.getList()
                .onRight { tags ->
                    val entities = tags.map { it.toEntity() }
                    database.withTransaction {
                        tagDao.deleteAll()
                        tagDao.upsert(entities)
                    }
                }
        }
    )
}
