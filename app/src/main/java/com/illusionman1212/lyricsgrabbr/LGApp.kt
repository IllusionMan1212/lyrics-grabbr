package com.illusionman1212.lyricsgrabbr

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.illusionman1212.lyricsgrabbr.data.HomeRepository
import com.illusionman1212.lyricsgrabbr.data.LyricsRepository
import com.illusionman1212.lyricsgrabbr.data.SettingsPreferencesRepository

private const val PREFERENCES_NAME = "preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME
)

class LGApp: Application() {
    lateinit var settingsPreferencesRepository: SettingsPreferencesRepository
    lateinit var homeRepository: HomeRepository
    lateinit var lyricsRepository: LyricsRepository

    override fun onCreate() {
        super.onCreate()
        settingsPreferencesRepository = SettingsPreferencesRepository(dataStore)
        homeRepository = HomeRepository(applicationContext)
        lyricsRepository = LyricsRepository()
    }
}
