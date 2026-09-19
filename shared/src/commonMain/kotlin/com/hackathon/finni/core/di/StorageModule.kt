package com.hackathon.finni.core.di

import androidx.datastore.core.okio.OkioSerializer
import com.hackathon.finni.data.storage.AppSettingsStorage
import com.hackathon.finni.data.storage.AuthSettings
import com.hackathon.finni.data.storage.AuthSettingsSerializer
import com.hackathon.finni.data.storage.AuthSettingsStorage
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val storageModule = module {
    singleOf(::AppSettingsStorage)

    single<OkioSerializer<AuthSettings>> {
        AuthSettingsSerializer(get()) 
    }
    single { AuthSettingsStorage(get(named("auth_datastore"))) }
}
