package com.hackathon.finni.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isOnboardingCompleted: Boolean = false
)

class AppSettingsStorage(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
    }

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[THEME_MODE]?.let { name ->
                ThemeMode.valueOf(name)
            } ?: ThemeMode.SYSTEM,
            isOnboardingCompleted = prefs[IS_ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { prefs ->
            val current = AppSettings(
                themeMode = prefs[THEME_MODE]?.let { name ->
                    runCatching { ThemeMode.valueOf(name) }.getOrDefault(ThemeMode.SYSTEM)
                } ?: ThemeMode.SYSTEM,
                isOnboardingCompleted = prefs[IS_ONBOARDING_COMPLETED] ?: false
            )
            val updated = transform(current)

            prefs[THEME_MODE] = updated.themeMode.name
            prefs[IS_ONBOARDING_COMPLETED] = updated.isOnboardingCompleted
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}