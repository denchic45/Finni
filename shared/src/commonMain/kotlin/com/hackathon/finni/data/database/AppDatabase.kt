package com.hackathon.finni.data.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.hackathon.finni.data.database.dao.NoteDao
import com.hackathon.finni.data.database.dao.ProjectDao
import com.hackathon.finni.data.database.dao.SyncQueueDao
import com.hackathon.finni.data.database.dao.TagDao
import com.hackathon.finni.data.database.dao.TaskDao
import com.hackathon.finni.data.database.entity.NoteEntity
import com.hackathon.finni.data.database.entity.ProjectEntity
import com.hackathon.finni.data.database.entity.SyncQueueEntity
import com.hackathon.finni.data.database.entity.TagEntity
import com.hackathon.finni.data.database.entity.TaskEntity

@ColumnTypeConverters(DatabaseConverters::class)
@Database(
    entities = [
        ProjectEntity::class,
        TaskEntity::class,
        NoteEntity::class,
        TagEntity::class,
        SyncQueueEntity::class
    ],
    version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun tagDao(): TagDao
    abstract fun syncQueueDao(): SyncQueueDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
