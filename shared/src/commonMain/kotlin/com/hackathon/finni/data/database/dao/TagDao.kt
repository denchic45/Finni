package com.hackathon.finni.data.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import com.hackathon.finni.api.tag.model.TagId
import com.hackathon.finni.data.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Upsert
    suspend fun upsert(tag: TagEntity)

    @Upsert
    suspend fun upsert(tags: List<TagEntity>)

    @Update
    suspend fun update(tag: TagEntity)

    @Query("DELETE FROM tags WHERE tag_id = :tagId")
    suspend fun delete(tagId: TagId)

    @Query("SELECT * FROM tags")
    fun getAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE tag_id = :id")
    suspend fun getById(id: TagId): TagEntity?

    @Query("SELECT * FROM tags WHERE tag_id = :id")
    fun observeById(id: TagId): Flow<TagEntity>
    
    @Query("DELETE FROM tags")
    suspend fun deleteAll()
}
