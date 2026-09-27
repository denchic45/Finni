package com.hackathon.finni.data.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey
    val id: Int,
    val number: Int,
    val title: String,
    val description: String = "",
    @ColumnInfo(name = "chapter_number")
    val chapterNumber: Int = 1,
    @ColumnInfo(name = "chapter_title")
    val chapterTitle: String = "",
    val status: String = "LOCKED", // "LOCKED", "CURRENT", "COMPLETED"
    val stars: Int = 0,
    @ColumnInfo(name = "reward_coins")
    val rewardCoins: Int = 25
)
