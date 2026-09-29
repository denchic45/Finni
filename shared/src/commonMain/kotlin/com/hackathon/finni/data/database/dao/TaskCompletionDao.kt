package com.hackathon.finni.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.hackathon.finni.data.database.entity.TaskCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskCompletionDao {
    @Query("SELECT * FROM task_completion WHERE taskId = :taskId")
    suspend fun get(taskId: String): TaskCompletionEntity?

    @Query("SELECT * FROM task_completion WHERE taskId = :taskId")
    fun observe(taskId: String): Flow<TaskCompletionEntity?>

    @Insert
    suspend fun insert(completion: TaskCompletionEntity)

    @Query("DELETE FROM task_completion")
    suspend fun clear()
}
