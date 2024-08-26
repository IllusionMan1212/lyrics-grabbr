package com.illusionman1212.lyricsgrabbr.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.map

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM,
}

data class SettingsPreferences(
    val appTheme: Int = Theme.SYSTEM.ordinal,
    val whitelist: Set<String> = emptySet(),
    val keepScreenOn: Boolean = false,
    val whitelistPromptSeen: Boolean = false,
    val geniURLbaseURL: String = "https://api.illusionman1212.com/lyrics",
)

class SettingsPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val APP_THEME = intPreferencesKey("app_theme")
        private val WHITELIST = stringSetPreferencesKey("apps_whitelist")
        private val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        private val WHITELIST_PROMPT_SEEN = booleanPreferencesKey("whitelist_prompt_seen")
        private val GENIURL_BASE_URL = stringPreferencesKey("geniurl_base_url")
    }

    val preferences = dataStore.data.map { mapSettingsPreferences(it) }

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { prefs ->
            prefs[APP_THEME] = theme.ordinal
        }
    }

    suspend fun addToWhitelist(packageName: String) {
        dataStore.edit { prefs ->
            prefs[WHITELIST] = (prefs[WHITELIST]?.toMutableSet() ?: emptySet()) + packageName
        }
    }

    suspend fun toggleKeepScreenOn() {
        dataStore.edit { prefs ->
            prefs[KEEP_SCREEN_ON] = !(prefs[KEEP_SCREEN_ON] ?: false)
        }
    }

    suspend fun removeFromWhitelist(packageName: String) {
        dataStore.edit { prefs ->
            prefs[WHITELIST] = (prefs[WHITELIST]?.toMutableSet() ?: emptySet()) - packageName
        }
    }

    suspend fun discardWhitelistPrompt() {
        dataStore.edit { prefs ->
            prefs[WHITELIST_PROMPT_SEEN] = true
        }
    }

    suspend fun setGeniURLBaseURL(url: String) {
        dataStore.edit { prefs ->
            prefs[GENIURL_BASE_URL] = url
        }
    }

    private fun mapSettingsPreferences(prefs: Preferences): SettingsPreferences {
        return SettingsPreferences(
            prefs[APP_THEME] ?: Theme.SYSTEM.ordinal,
            prefs[WHITELIST] ?: emptySet(),
            prefs[KEEP_SCREEN_ON] ?: false,
            prefs[WHITELIST_PROMPT_SEEN] ?: false,
            prefs[GENIURL_BASE_URL] ?: "https://api.illusionman1212.com/lyrics",
        )
    }
}