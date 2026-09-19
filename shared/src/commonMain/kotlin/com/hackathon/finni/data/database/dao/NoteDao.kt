package com.hackathon.finni.data.database.dao

import androidx.room3.Dao
import androidx.room3.MapColumn
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.Upsert
import com.hackathon.finni.api.note.model.NoteId
import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.data.database.entity.NoteEntity
import com.hackathon.finni.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Upsert
    suspend fun upsert(notes: List<NoteEntity>)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("SELECT global_index FROM notes WHERE note_id = :id")
    suspend fun getGlobalIndexOrNull(id: NoteId): Int?

    @Query("UPDATE notes SET is_deleted = 1, sync_status = :syncStatus WHERE note_id = :noteId")
    suspend fun softDelete(noteId: NoteId, syncStatus: SyncStatus)

    @Query("DELETE FROM notes WHERE note_id = :noteId")
    suspend fun delete(noteId: NoteId)

    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE note_id = :id")
    suspend fun getById(id: NoteId): NoteEntity?

    @Query("SELECT * FROM notes WHERE note_id = :id")
    fun observeById(id: NoteId): Flow<NoteEntity>

    @Query(
        """
    SELECT 
        ((global_index / :pageSize) + 1) AS page,
        *
    FROM notes
    WHERE ((global_index / :pageSize) + 1) IN (:pages)
      AND (:searchQuery IS NULL OR title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%')
      AND (:projectId IS NULL OR project_id = :projectId)
      AND (:isArchived IS NULL OR is_archived = :isArchived)
      AND (:isDeleted IS NULL OR is_deleted = :isDeleted)
      AND (:hasReminder IS NULL OR reminder IS NOT NULL)
    ORDER BY 
        CASE WHEN :sortOrder = 'CREATED_DESC' THEN created_at END DESC,
        CASE WHEN :sortOrder = 'CREATED_ASC' THEN created_at END ASC,
        CASE WHEN :sortOrder = 'UPDATED_DESC' THEN updated_at END DESC,
        global_index ASC
"""
    )
    fun observeByPages(
        pages: Set<Int>,
        pageSize: Int,
        searchQuery: String? = null,
        projectId: ProjectId? = null,
        isArchived: Boolean? = null,
        isDeleted: Boolean? = null,
        hasReminder: Boolean = false,
        sortOrder: String = "MANUAL"
    ): Flow<Map<@MapColumn(columnName = "page") Int, List<NoteEntity>>>

    @Query("UPDATE notes SET global_index = global_index + 1")
    suspend fun incrementGlobalIndexes()

    @Query("DELETE FROM notes WHERE global_index BETWEEN :fromIndex AND :toIndex")
    suspend fun deleteRange(fromIndex: Int, toIndex: Int)

    @Transaction
    suspend fun replacePage(fromIndex: Int, toIndex: Int, entities: List<NoteEntity>) {
        deleteRange(fromIndex, toIndex)
        upsert(entities)
    }

    @Query("SELECT COUNT(*) FROM notes WHERE project_id IS NULL AND is_archived = 0 AND is_deleted = 0")
    fun countInbox(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE is_archived = 0 AND is_deleted = 0")
    fun countAll(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE is_deleted = 1")
    fun countTrash(): Flow<Int>

    @Query("""
        SELECT project_id, COUNT(*) as count FROM notes WHERE is_archived = 0
        AND is_deleted = 0
        AND project_id IS NOT NULL GROUP BY project_id
    """)
    fun countByProjects(): Flow<Map<@MapColumn(columnName = "project_id") ProjectId, @MapColumn(columnName = "count") Int>>
}
