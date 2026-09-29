package com.hackathon.finni.data.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "task_completion")
data class TaskCompletionEntity(
    @PrimaryKey val taskId: String,
    val choiceId: String,
    val walletDelta: Int,
    val savingsDelta: Int,
    val mood: String,
    val isOptimal: Boolean
)
