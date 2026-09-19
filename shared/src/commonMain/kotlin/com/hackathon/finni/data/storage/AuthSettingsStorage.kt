package com.hackathon.finni.data.storage

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class AuthSettings(
    val token: String? = null,
    val refreshToken: String? = null
)

class AuthSettingsStorage(
    private val dataStore: DataStore<AuthSettings>
) {
    val settings: Flow<AuthSettings> = dataStore.data
    val token: Flow<String?> = settings.map { it.token }
    val refreshToken: Flow<String?> = settings.map { it.refreshToken }

    suspend fun updateSettings(transform: (AuthSettings) -> AuthSettings) {
        dataStore.updateData(transform)
    }

    suspend fun clear() {
        dataStore.updateData { AuthSettings() }
    }
}
