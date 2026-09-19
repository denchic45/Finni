package com.hackathon.finni.data.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.hackathon.finni.api.project.model.ProjectId


@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    @ColumnInfo(name = "project_id")
    val projectId: ProjectId,
    val name: String,
    val description: String,
    @ColumnInfo(name = "global_index")
    val globalIndex: Int
)