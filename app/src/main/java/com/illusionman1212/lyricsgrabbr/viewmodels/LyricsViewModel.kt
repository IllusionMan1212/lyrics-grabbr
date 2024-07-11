package com.illusionman1212.lyricsgrabbr.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.illusionman1212.lyricsgrabbr.LGApp
import com.illusionman1212.lyricsgrabbr.data.LyricsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LyricsState(
    // set by Home screen
    val song: String = "",
    val artist: String = "",
    val url: Uri = Uri.EMPTY,
    val trackId: String = "",

    // set by Lyrics screen
    val lyrics: String = "",
    val isLoading: Boolean = true,

    val error: String? = null,
)

class LyricsViewModel(private val lyricsRepository: LyricsRepository): ViewModel() {
    private val _uiState = MutableStateFlow(LyricsState())
    val uiState = _uiState.asStateFlow()

    fun onSongChange(trackId: String, title: String, subtitle: String, url: Uri) {
        _uiState.update {
            it.copy(song = title, artist = subtitle, url = url, trackId = trackId)
        }
    }

    fun fetchLyricsFromGenius() {
        _uiState.value = _uiState.value.copy(isLoading = true, lyrics = "")

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val lyrics = lyricsRepository.fetchLyricsFromGenius(uiState.value.url)
                _uiState.value = _uiState.value.copy(isLoading = false, lyrics = lyrics)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as LGApp)
                LyricsViewModel(application.lyricsRepository)
            }
        }
    }
}