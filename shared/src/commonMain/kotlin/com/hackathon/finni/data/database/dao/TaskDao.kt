package com.hackathon.finni.data.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.api.task.model.TaskId
import com.hackathon.finni.data.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE project_id = :projectId")
    fun getTasksForProject(projectId: ProjectId): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE task_id = :taskId")
    suspend fun getTaskById(taskId: TaskId): TaskEntity?
}
