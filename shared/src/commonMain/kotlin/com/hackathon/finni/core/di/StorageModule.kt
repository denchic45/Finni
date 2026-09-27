package com.hackathon.finni.core.di

import com.hackathon.finni.data.storage.AppSettingsStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val storageModule = module {
    singleOf(::AppSettingsStorage)
}
