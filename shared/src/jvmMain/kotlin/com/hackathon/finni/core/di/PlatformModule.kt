package com.hackathon.finni.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import com.google.crypto.tink.Aead
import com.hackathon.finni.core.crypto.JvmCryptoManager
import com.hackathon.finni.core.crypto.Tink
import com.hackathon.finni.core.lifecycle.AppLifecycleObserver
import com.hackathon.finni.core.network.DesktopNetworkObserver
import com.hackathon.finni.core.network.NetworkObserver
import com.hackathon.finni.core.presentation.handlers.UrlHandler
import com.hackathon.finni.core.ui.components.ToastManager
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.storage.AuthSettings
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {
    single {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "app_database.db")
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath
        )
    }
    single<DataStore<Preferences>> {
        val dsFile = File(System.getProperty("java.io.tmpdir"), "app_settings.preferences_pb")
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { dsFile.absolutePath.toPath() }
        )
    }
    single<Aead> { Tink.createAead() }
    singleOf(::JvmCryptoManager)
    single<DataStore<AuthSettings>>(named("auth_datastore")) {
        DataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = get<OkioSerializer<AuthSettings>>(),
                producePath = {
                    File(
                        System.getProperty("java.io.tmpdir"),
                        "auth_settings.pb"
                    ).absolutePath.toPath()
                }
            )
        )
    }
    singleOf(::UrlHandler)
    singleOf(::ToastManager)
    singleOf(::AppLifecycleObserver)
    singleOf(::DesktopNetworkObserver) { bind<NetworkObserver>() }
}
