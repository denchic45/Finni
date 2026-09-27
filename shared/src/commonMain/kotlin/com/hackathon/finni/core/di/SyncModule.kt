package com.hackathon.finni.core.di

import com.hackathon.finni.data.sync.OfflineEntityHandler
import com.hackathon.finni.data.sync.SyncManager
import org.koin.dsl.module

val syncModule = module {
    single {
        val handlers = emptyMap<String, OfflineEntityHandler<*, *>>()
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

