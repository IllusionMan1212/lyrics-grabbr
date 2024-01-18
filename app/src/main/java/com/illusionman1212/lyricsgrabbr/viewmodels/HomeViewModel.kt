package com.illusionman1212.lyricsgrabbr.viewmodels

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.adamratzman.spotify.SpotifyClientApi
import com.illusionman1212.lyricsgrabbr.LGApp
import com.illusionman1212.lyricsgrabbr.NotificationEvent
import com.illusionman1212.lyricsgrabbr.R
import com.illusionman1212.lyricsgrabbr.data.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class HomeViewModel(private val context: Context, private val homeRepository: HomeRepository): ViewModel() {
    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun getListeningPermission(): Boolean {
        return homeRepository.isPermissionGranted()
    }

    fun setSong(song: NotificationEvent) {
        _uiState.value = _uiState.value.copy(isLoading = true, notification = song, error = null)
    }

    fun makeRequestToSpotify(spotifyApi: SpotifyClientApi) {
        viewModelScope.launch {
            val track = spotifyApi.player.getCurrentlyPlaying()?.item

            if (track?.id == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = context.resources.getString(R.string.no_spotify_song_playing)
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    results = listOf(SearchResult(
                        title = _uiState.value.notification!!.title,
                        artist = _uiState.value.notification!!.artist,
                        url = track.asTrack?.externalUrls?.spotify?.toUri() ?: Uri.EMPTY,
                        artistUrl = track.asTrack?.artists?.get(0)?.externalUrls?.spotify?.toUri() ?: Uri.EMPTY,
                        thumbnail = track.asTrack?.album?.images?.get(0)?.url?.toUri() ?: Uri.EMPTY,
                        id = track.id!!,
                    ))
                )
            }
        }
    }

    fun makeRequestToGenius(song: String, artist: String) {
        viewModelScope.launch {
            _uiState.update { HomeState(true, uiState.value.results, uiState.value.notification, null) }

            withContext(Dispatchers.IO) {
                val (res, isSuccess) = homeRepository.makeSearchRequest(song, artist)
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
                HomeViewModel(application.applicationContext, application.homeRepository)
            }
        }
    }
}