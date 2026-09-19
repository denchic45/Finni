package com.hackathon.finni.data.service

import com.hackathon.finni.api.profile.AuthApi
import com.hackathon.finni.api.profile.model.LoginRequest
import com.hackathon.finni.api.profile.model.RegisterRequest
import com.hackathon.finni.core.di.ApplicationScope
import com.hackathon.finni.core.network.safeFetch
import com.hackathon.finni.data.database.AppDatabase
import com.hackathon.finni.data.storage.AppSettingsStorage
import com.hackathon.finni.data.storage.AuthSettingsStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class AuthService(
    coroutineScope: ApplicationScope,
    private val authApi: AuthApi,
    private val authStorage: AuthSettingsStorage,
    private val appSettingsStorage: AppSettingsStorage,
    private val database: AppDatabase
) {

    val isAuthorized: Flow<Boolean> = authStorage.token.map { it != null }

    init {
        isAuthorized
            .distinctUntilChanged()
            .filter { !it }
            .onEach { logout() }
            .launchIn(coroutineScope)
    }

    suspend fun login(request: LoginRequest) = safeFetch {
        authApi.login(request)
    }.onRight { response ->
        authStorage.updateSettings {
            it.copy(
                token = response.accessToken,
                refreshToken = response.refreshToken
            )
        }
    }

    suspend fun register(request: RegisterRequest) = safeFetch {
        authApi.register(request)
    }.onRight { response ->
        authStorage.updateSettings {
            it.copy(
                token = response.accessToken,
                refreshToken = response.refreshToken
            )
        }
    }

    suspend fun logout() {
        authStorage.clear()
        appSettingsStorage.clear()
        database.clearAllTables()
    }
}
