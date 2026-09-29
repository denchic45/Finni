package com.hackathon.finni.core.di

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.hackathon.finni.data.database.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        val builder = get<RoomDatabase.Builder<AppDatabase>>()
        builder.setDriver(BundledSQLiteDriver()).build()
    }

    single { get<AppDatabase>().syncQueueDao() }
    single { get<AppDatabase>().petStateDao() }
    single { get<AppDatabase>().accountDao() }
    single { get<AppDatabase>().levelProgressDao() }
}
