package com.illusionman1212.lyricsgrabbr

sealed class Routes(val route: String) {
    object Home : Routes("home")
    object Lyrics : Routes("lyrics")
    object Settings : Routes("settings")
}

