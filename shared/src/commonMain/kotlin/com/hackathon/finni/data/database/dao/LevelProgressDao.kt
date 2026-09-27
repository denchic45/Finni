package com.hackathon.finni.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.hackathon.finni.data.database.entity.LevelProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress ORDER BY number ASC")
    fun observeLevels(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress ORDER BY number ASC")
    suspend fun getAllLevels(): List<LevelProgressEntity>

    @Query("SELECT * FROM level_progress WHERE id = :id LIMIT 1")
    suspend fun getLevel(id: Int): LevelProgressEntity?

    @Query("SELECT COUNT(*) FROM level_progress")
    suspend fun getLevelsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLevels(levels: List<LevelProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLevel(level: LevelProgressEntity)
}
