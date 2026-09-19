package com.hackathon.finni.data.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.api.task.model.TaskId

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["project_id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["project_id"])]
)
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "task_id")
    val taskId: TaskId,
    @ColumnInfo(name = "project_id")
    val projectId: ProjectId,
    val title: String,
    val description: String,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false
)
