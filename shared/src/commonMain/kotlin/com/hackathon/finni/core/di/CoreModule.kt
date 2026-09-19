package com.hackathon.finni.core.di

import com.hackathon.finni.core.presentation.handlers.ErrorHandler
import com.hackathon.finni.core.presentation.handlers.ErrorHandlerImpl
import com.hackathon.finni.core.presentation.handlers.EventHandler
import com.hackathon.finni.core.presentation.handlers.LoadingHandler
import com.hackathon.finni.core.presentation.handlers.LoadingHandlerImpl
import com.hackathon.finni.core.presentation.handlers.RefreshHandler
import com.hackathon.finni.core.presentation.handlers.RefreshHandlerImpl
import com.hackathon.finni.core.ui.navigation.Splash
import com.hackathon.finni.core.ui.navigation.router.Router
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

typealias ApplicationScope = CoroutineScope

val coreModule = module {
    single<ApplicationScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
//    factory { (tag: String) -> Logger.withTag(tag) }
    single { Router(Splash) }
    singleOf(::EventHandler)
    singleOf(::LoadingHandlerImpl) { bind<LoadingHandler>() }
    singleOf(::ErrorHandlerImpl) { bind<ErrorHandler>() }
    factoryOf(::RefreshHandlerImpl) { bind<RefreshHandler>() }
}
