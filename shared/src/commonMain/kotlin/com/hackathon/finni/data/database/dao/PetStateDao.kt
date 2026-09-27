package com.hackathon.finni.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.hackathon.finni.data.database.entity.PetStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetStateDao {
    @Query("SELECT * FROM pet_state WHERE id = 1")
    fun observePetState(): Flow<PetStateEntity?>

    @Query("SELECT * FROM pet_state WHERE id = 1")
    suspend fun getPetState(): PetStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPetState(state: PetStateEntity)
}
