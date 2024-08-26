package com.illusionman1212.lyricsgrabbr.viewmodels

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppInfo(
    val displayName: String,
    val icon: Drawable,
    var isWhitelisted: Boolean,
)

data class SettingsState(
    val appTheme: Int = Theme.SYSTEM.ordinal,
    val applications: Map<String, AppInfo> = emptyMap(),
    val whitelistSearchQuery: String = "",
    val keepScreenOn: Boolean = false,
    val geniURLbaseURL: String = "",
    val whitelistPromptSeen: Boolean = true,
)

class SettingsViewModel(private val settingsPrefsRepo: SettingsPreferencesRepository, context: Context): ViewModel() {
    private var immutablePackages: Map<String, AppInfo> = emptyMap()
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

    fun discardWhitelistPrompt() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.discardWhitelistPrompt()
        }
    }

    fun setGeniURLBaseURL(baseUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.setGeniURLBaseURL(baseUrl)
            _uiState.update { state ->
                state.copy(geniURLbaseURL = baseUrl)
            }
        }
    }

    fun toggleKeepScreenOn() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsPrefsRepo.toggleKeepScreenOn()
        }
    }

    private suspend fun getWhitelistedApps(): Set<String> {
        return settingsPrefsRepo.preferences.first().whitelist
    }

    private fun getPackages(context: Context) {
        if (immutablePackages.isNotEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.packageName != context.packageName && pm.getLaunchIntentForPackage(it.packageName) != null }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.loadLabel(pm).toString() })

            val apps: MutableMap<String, AppInfo> = mutableMapOf()
            val whitelistedApps = getWhitelistedApps()

            packages.forEach { pkg ->
                val label = pkg.loadLabel(pm)
                val icon = pkg.loadIcon(pm)
                apps[pkg.packageName] = AppInfo(
                    displayName = label.toString(),
                    icon = icon,
                    isWhitelisted = pkg.packageName in whitelistedApps
                )
            }
            immutablePackages = apps
            _uiState.update { state ->
                state.copy(
                    applications = apps
                )
            }
        }
    }

    fun refreshPackages(context: Context) {
        _uiState.update {
            val apps = uiState.value.applications.toMutableMap()
            apps.clear()
            it.copy(whitelistSearchQuery = "", applications = apps)
        }
        val apps = immutablePackages.toMutableMap()
        apps.clear()
        immutablePackages = apps
        getPackages(context)
    }

    fun filterPackages(query: String) {
        _uiState.update { state ->
            state.copy(
                applications = immutablePackages.filter {
                    it.value.displayName.lowercase().contains(query.lowercase())
                }.toMutableMap(),
                whitelistSearchQuery = query,
            )
        }
    }

    fun clearWhitelistSearch() {
        _uiState.update {
            it.copy(applications = immutablePackages, whitelistSearchQuery = "")
        }
    }

    fun addToWhitelist(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state ->
                val apps = immutablePackages.toMutableMap()
                apps[packageName] = apps[packageName]!!.copy(isWhitelisted = true)
                immutablePackages = apps
                state.copy(
                    applications = immutablePackages.filter {
                        it.value.displayName.lowercase().contains(uiState.value.whitelistSearchQuery.lowercase())
                    }.toMutableMap(),
                )
            }
            settingsPrefsRepo.addToWhitelist(packageName)
        }
    }

    fun removeFromWhitelist(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state ->
                val apps = immutablePackages.toMutableMap()
                apps[packageName] = apps[packageName]!!.copy(isWhitelisted = false)
                immutablePackages = apps
                state.copy(
                    applications = immutablePackages.filter {
                        it.value.displayName.lowercase().contains(uiState.value.whitelistSearchQuery.lowercase())
                    }.toMutableMap(),
                )
            }
            settingsPrefsRepo.removeFromWhitelist(packageName)
        }
    }

    init {
        getPackages(context)
        viewModelScope.launch {
            settingsPrefsRepo.preferences.collectLatest {
                _uiState.update { state ->
                    state.copy(
                        appTheme = it.appTheme,
                        keepScreenOn = it.keepScreenOn,
                        whitelistPromptSeen = it.whitelistPromptSeen,
                        geniURLbaseURL = it.geniURLbaseURL,
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as LGApp)
                SettingsViewModel(application.settingsPreferencesRepository, application.applicationContext)
            }
        }
    }
}
