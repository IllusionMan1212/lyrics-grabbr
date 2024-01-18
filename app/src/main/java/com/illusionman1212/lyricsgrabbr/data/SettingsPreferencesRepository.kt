package com.illusionman1212.lyricsgrabbr.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.map

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM,
}

data class SettingsPreferences(
    val appTheme: Int = Theme.SYSTEM.ordinal,
)

class SettingsPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val APP_THEME = intPreferencesKey("app_theme")
    }

    val preferences = dataStore.data.map { mapSettingsPreferences(it) }

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { prefs ->
            prefs[APP_THEME] = theme.ordinal
        }
    }

    private fun mapSettingsPreferences(prefs: Preferences): SettingsPreferences {
        return SettingsPreferences(
            prefs[APP_THEME] ?: Theme.SYSTEM.ordinal,
        )
    }
}