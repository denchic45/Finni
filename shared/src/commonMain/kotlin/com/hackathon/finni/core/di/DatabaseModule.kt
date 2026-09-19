package com.hackathon.finni.core.di

import androidx.room3.RoomDatabase
import com.hackathon.finni.data.database.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        val builder = get<RoomDatabase.Builder<AppDatabase>>()
        builder.build()
    }

    single { get<AppDatabase>().projectDao() }
    single { get<AppDatabase>().taskDao() }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().tagDao() }
    single { get<AppDatabase>().syncQueueDao() }
}
