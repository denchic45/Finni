package com.hackathon.finni.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import com.hackathon.finni.core.crypto.AndroidCryptoManager
import com.hackathon.finni.core.crypto.CryptoManager
import com.hackathon.finni.core.crypto.Tink
import com.hackathon.finni.core.lifecycle.AppLifecycleObserver
import com.hackathon.finni.core.network.AndroidNetworkObserver
import com.hackathon.finni.core.network.NetworkObserver
import com.hackathon.finni.core.presentation.handlers.UrlHandler
import com.hackathon.finni.core.ui.components.ToastManager
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.storage.AuthSettings
import com.google.crypto.tink.Aead
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = "database"
        )
    }
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                androidContext().filesDir.resolve("app_settings.preferences_pb").absolutePath.toPath()
            }
        )
    }
    single<Aead> { Tink.createAead(androidContext()) }
    single<CryptoManager> { AndroidCryptoManager(get()) }
    single<DataStore<AuthSettings>>(named("auth_datastore")) {
        DataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = get<OkioSerializer<AuthSettings>>(),
                producePath = {
                    androidContext().filesDir.resolve("auth_settings.pb").absolutePath.toPath()
                }
            )
        )
    }
    singleOf(::UrlHandler)
    singleOf(::ToastManager)
    singleOf(::AppLifecycleObserver)
    single<NetworkObserver> { AndroidNetworkObserver(androidContext(), get()) }
}
