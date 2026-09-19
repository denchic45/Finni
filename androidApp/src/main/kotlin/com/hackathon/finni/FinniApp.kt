package com.hackathon.finni

import android.app.Application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import co.touchlab.kermit.platformLogWriter
import com.hackathon.finni.core.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FinniApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@FinniApp)
            logger(
                KermitKoinLogger(Logger.withTag("koin"))
            )
            modules(appModules)
        }

        Logger.setLogWriters(
            platformLogWriter()
        )
    }
}
