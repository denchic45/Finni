package com.hackathon.finni.data.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "pet_state")
data class PetStateEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Финни",
    val hunger: Int = 4, // 0..5
    val mood: String = "Neutral", // "Sad", "Neutral", "Happy"
    @ColumnInfo(name = "time_tokens")
    val timeTokens: Int = 3, // 0..3
    @ColumnInfo(name = "time_phase")
    val timePhase: String = "Morning", // "Morning", "Day", "Evening", "Night"
    @ColumnInfo(name = "current_chapter")
    val currentChapter: Int = 1,
    @ColumnInfo(name = "current_day")
    val currentDay: Int = 1
)
