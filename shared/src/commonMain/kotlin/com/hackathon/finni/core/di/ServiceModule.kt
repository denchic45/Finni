package com.hackathon.finni.core.di

import com.hackathon.finni.data.service.AuthService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val serviceModule = module {
    singleOf(::AuthService)
}
