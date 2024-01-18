package com.illusionman1212.lyricsgrabbr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.illusionman1212.lyricsgrabbr.LGApp
import com.illusionman1212.lyricsgrabbr.data.SettingsPreferencesRepository
import com.illusionman1212.lyricsgrabbr.data.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsState(
    val appTheme: Int = Theme.SYSTEM.ordinal,
    val isSpotifyAuthed: Boolean = false,
)

class SettingsViewModel(private val settingsPrefsRepo: SettingsPreferencesRepository): ViewModel() {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    suspend fun getAppTheme(): Int {
        return settingsPrefsRepo.preferences.first().appTheme
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setTheme(theme)
        }
    }

    fun updateSpotifyAuthStatus(isAuthed: Boolean) {
        _uiState.value = _uiState.value.copy(isSpotifyAuthed = isAuthed)
    }

    init {
        viewModelScope.launch {
            settingsPrefsRepo.preferences.collectLatest {
                _uiState.value = _uiState.value.copy(
                    appTheme = it.appTheme,
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as LGApp)
                SettingsViewModel(application.settingsPreferencesRepository)
            }
        }
    }
}
