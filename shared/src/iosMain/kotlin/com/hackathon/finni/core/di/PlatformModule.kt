package com.hackathon.finni.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import com.hackathon.finni.core.crypto.CryptoManager
import com.hackathon.finni.core.crypto.IosCryptoManager
import com.hackathon.finni.core.lifecycle.AppLifecycleObserver
import com.hackathon.finni.core.network.IosNetworkObserver
import com.hackathon.finni.core.network.NetworkObserver
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.storage.AuthSettings
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module = module {
    single {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val dbFilePath = requireNotNull(documentDirectory?.path) + "/app_database.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath
        )
    }

    single<DataStore<Preferences>> {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val path = requireNotNull(documentDirectory?.path) + "/app_settings.preferences_pb"
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { path.toPath() }
        )
    }

    single<CryptoManager> { IosCryptoManager() }

    single<DataStore<AuthSettings>>(named("auth_datastore")) {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val path = requireNotNull(documentDirectory?.path) + "/auth_settings.pb"
        DataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = get<OkioSerializer<AuthSettings>>(),
                producePath = { path.toPath() }
            )
        )
    }

    single { AppLifecycleObserver() }
    single<NetworkObserver> { IosNetworkObserver() }
}
