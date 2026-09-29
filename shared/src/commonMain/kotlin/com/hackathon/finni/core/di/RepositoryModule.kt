package com.hackathon.finni.core.di

import com.hackathon.finni.data.repository.GameStateRepository
import com.hackathon.finni.data.repository.GameStateRepositoryImpl
import com.hackathon.finni.data.repository.TasksRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { TasksRepository(get()) }
    single<GameStateRepository> {
        GameStateRepositoryImpl(
            database = get(),
            petStateDao = get(),
            accountDao = get(),
            levelProgressDao = get()
        )
    }
}

