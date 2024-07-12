package com.illusionman1212.lyricsgrabbr

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Lyrics : Routes("lyrics")
    data object Settings : Routes("settings")
    data object Whitelist : Routes("settings/whitelist")
}

