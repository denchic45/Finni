package com.hackathon.finni.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.hackathon.finni.data.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM account_state WHERE id = 1")
    fun observeAccountState(): Flow<AccountEntity?>

    @Query("SELECT * FROM account_state WHERE id = 1")
    suspend fun getAccountState(): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccountState(account: AccountEntity)
}
