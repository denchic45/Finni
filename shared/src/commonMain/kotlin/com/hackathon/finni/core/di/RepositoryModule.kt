package com.hackathon.finni.core.di

import com.hackathon.finni.data.repository.GameStateRepository
import com.hackathon.finni.data.repository.GameStateRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<GameStateRepository> {
        GameStateRepositoryImpl(
            petStateDao = get(),
            accountDao = get(),
            levelProgressDao = get()
        )
    }
}

