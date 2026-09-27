package com.hackathon.finni.data.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.hackathon.finni.data.database.dao.AccountDao
import com.hackathon.finni.data.database.dao.LevelProgressDao
import com.hackathon.finni.data.database.dao.PetStateDao
import com.hackathon.finni.data.database.dao.SyncQueueDao
import com.hackathon.finni.data.database.entity.AccountEntity
import com.hackathon.finni.data.database.entity.LevelProgressEntity
import com.hackathon.finni.data.database.entity.PetStateEntity
import com.hackathon.finni.data.database.entity.SyncQueueEntity

@ColumnTypeConverters(DatabaseConverters::class)
@Database(
    entities = [
        SyncQueueEntity::class,
        PetStateEntity::class,
        AccountEntity::class,
        LevelProgressEntity::class
    ],
    version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun petStateDao(): PetStateDao
    abstract fun accountDao(): AccountDao
    abstract fun levelProgressDao(): LevelProgressDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
