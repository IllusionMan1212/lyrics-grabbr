package com.illusionman1212.lyricsgrabbr.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.illusionman1212.lyricsgrabbr.LGApp
import com.illusionman1212.lyricsgrabbr.NotificationEvent
import com.illusionman1212.lyricsgrabbr.data.HomeRepository
import com.illusionman1212.lyricsgrabbr.data.SettingsPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

object UriSerializer : KSerializer<Uri> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): Uri {
        return Uri.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }
}

data class SearchResult(
    val title: String,
    val artist: String,
    val url: Uri,
    val artistUrl: Uri,
    val thumbnail: Uri,
    val id: String,
)

@Serializable
data class Resources(
    @Serializable(with = UriSerializer::class)
    val thumbnail: Uri,
)

@Serializable
data class Artist(
    val name: String,
    @Serializable(with = UriSerializer::class)
    val url: Uri,
)

@Serializable
data class Meta(
    val title: String,
    val primaryArtist: Artist,
)

@Serializable
data class IntermediateResult(
    @Serializable(with = UriSerializer::class)
    val url: Uri,
    val meta: Meta,
    val resources: Resources,
    val id: Int,
)

@Serializable
data class SearchResultsResponse(
    val all: List<IntermediateResult>
)

data class HomeState(
    val isLoading: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val notification: NotificationEvent? = null,
    val error: String? = null,
)

class HomeViewModel(private val homeRepository: HomeRepository, private val settingsRepository: SettingsPreferencesRepository): ViewModel() {
    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun getListeningPermission(): Boolean {
        return homeRepository.isPermissionGranted()
    }

    fun setSong(song: NotificationEvent) {
        _uiState.update { state ->
            state.copy(isLoading = true, notification = song, error = null)
        }
    }

    fun makeRequestToGenius(context: Context, song: String, artist: String) {
        viewModelScope.launch {
            _uiState.update { HomeState(true, uiState.value.results, uiState.value.notification, null) }

            withContext(Dispatchers.IO) {
                val baseUrl = settingsRepository.preferences.first().geniURLbaseURL
                val (res, isSuccess) = homeRepository.makeSearchRequest(context, baseUrl, song, artist)
                if (isSuccess) {
                    val results = json.decodeFromString(SearchResultsResponse.serializer(), res!!).all.map {
                        SearchResult(
                            title = it.meta.title,
                            artist = it.meta.primaryArtist.name,
                            url = it.url,
                            artistUrl = it.meta.primaryArtist.url,
                            thumbnail = it.resources.thumbnail,
                            id = it.id.toString()
                        )
                    }
                    _uiState.update { HomeState(false, results, uiState.value.notification, null) }
                } else {
                    _uiState.update { HomeState(
                        false,
                        emptyList(),
                        uiState.value.notification,
                        res
                    ) }
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as LGApp)
                HomeViewModel(application.homeRepository, application.settingsPreferencesRepository)
            }
        }
    }
}