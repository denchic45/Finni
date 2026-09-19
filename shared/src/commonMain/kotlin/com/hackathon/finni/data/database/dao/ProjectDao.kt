package com.hackathon.finni.data.database.dao

import androidx.room3.Dao
import androidx.room3.MapColumn
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.Upsert
import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.data.database.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ProjectDao {
    @Upsert
    suspend fun upsert(project: ProjectEntity)

    @Upsert
    suspend fun upsert(projects: List<ProjectEntity>)

    @Update
    suspend fun update(project: ProjectEntity)

    @Query("SELECT global_index FROM projects WHERE project_id = :id")
    suspend fun getGlobalIndexOrNull(id: ProjectId): Int?

    @Query("DELETE FROM projects WHERE project_id = :projectId")
    suspend fun delete(projectId: ProjectId)

    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE project_id = :id")
    suspend fun getById(id: ProjectId): ProjectEntity?

    @Query("SELECT * FROM projects WHERE project_id = :id")
    fun observeById(id: ProjectId): Flow<ProjectEntity>


    @Query(
        """
    SELECT 
        ((global_index / :pageSize) + 1) AS page,
        *
    FROM projects
    WHERE ((global_index / :pageSize) + 1) IN (:pages)
    ORDER BY global_index ASC
"""
    )
    fun observeByPages(
        pages: Set<Int>,
        pageSize: Int
    ): Flow<Map<@MapColumn(columnName = "page") Int, List<ProjectEntity>>>

    @Query("UPDATE projects SET global_index = global_index + 1")
    suspend fun incrementGlobalIndexes()

    @Query("DELETE FROM projects WHERE global_index BETWEEN :fromIndex AND :toIndex")
    suspend fun deleteRange(fromIndex: Int, toIndex: Int)

    @Transaction
    suspend fun replacePage(fromIndex: Int, toIndex: Int, entities: List<ProjectEntity>) {
        // 1. Очищаем старый диапазон этой страницы
        deleteRange(fromIndex, toIndex)
        // 2. Вставляем новые записи с актуальными globalIndex
        upsert(entities)
    }
}
