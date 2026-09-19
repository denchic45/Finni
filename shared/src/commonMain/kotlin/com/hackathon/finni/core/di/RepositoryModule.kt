package com.hackathon.finni.core.di

import com.hackathon.finni.data.repository.NoteRepository
import com.hackathon.finni.data.repository.ProjectRepository
import com.hackathon.finni.data.repository.TagRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::ProjectRepository)
    single {
        NoteRepository(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    singleOf(::TagRepository)
}
