package com.hackathon.finni.core.di

import com.hackathon.finni.data.sync.OfflineEntityHandler
import com.hackathon.finni.data.sync.SyncManager
import com.hackathon.finni.data.sync.NoteOfflineHandler
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val syncModule = module {
    singleOf(::NoteOfflineHandler)

    single {
        val noteHandler = get<NoteOfflineHandler>()
        val handlers = mapOf<String, OfflineEntityHandler<*, *>>(
            noteHandler.key to noteHandler
        )
        SyncManager(
            handlers,
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
