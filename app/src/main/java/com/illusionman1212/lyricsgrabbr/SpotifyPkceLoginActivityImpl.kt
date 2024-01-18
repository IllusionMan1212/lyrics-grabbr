package com.illusionman1212.lyricsgrabbr

import android.widget.Toast
import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.SpotifyScope
import com.adamratzman.spotify.auth.pkce.AbstractSpotifyPkceLoginActivity
import com.illusionman1212.lyricsgrabbr.viewmodels.SettingsViewModel

class SpotifyPkceLoginActivityImpl: AbstractSpotifyPkceLoginActivity() {
    override val clientId: String = CLIENT_ID
    override val redirectUri: String = SPOTIFY_REDIRECT_URI
    override val scopes: List<SpotifyScope> = listOf(SpotifyScope.UserReadCurrentlyPlaying)

    override fun onFailure(exception: Exception) {
        Toast.makeText(this, "Failed to authenticate", Toast.LENGTH_SHORT).show()
        settingsViewModel?.updateSpotifyAuthStatus(false)
    }

    override fun onSuccess(api: SpotifyClientApi) {
        (application as LGApp).credentialStore.setSpotifyApi(api)
        Toast.makeText(this, "Successfully authenticated", Toast.LENGTH_SHORT).show()
        settingsViewModel?.updateSpotifyAuthStatus(true)
    }

    companion object {
        var CLIENT_ID = ""
        var settingsViewModel: SettingsViewModel? = null
    }
}