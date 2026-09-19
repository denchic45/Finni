package com.hackathon.finni.core.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.kermitLoggerModule

val appModules = listOf(
    platformModule,
    coreModule,
    databaseModule,
    networkModule,
    repositoryModule,
    serviceModule,
    storageModule,
    syncModule,
    viewModelModule,
    kermitLoggerModule((Logger.withTag("koin")))
)