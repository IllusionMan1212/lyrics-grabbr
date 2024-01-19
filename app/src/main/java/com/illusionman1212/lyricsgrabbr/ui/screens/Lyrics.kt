package com.illusionman1212.lyricsgrabbr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.illusionman1212.lyricsgrabbr.R
import com.illusionman1212.lyricsgrabbr.ui.components.LGIconButton
import com.illusionman1212.lyricsgrabbr.utils.mirror
import com.illusionman1212.lyricsgrabbr.viewmodels.LyricsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsPage(
    goBack: () -> Unit,
    lyricsViewModel: LyricsViewModel = viewModel(
        factory = LyricsViewModel.Factory
    ),
) {
    val uiState = lyricsViewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(uiState.song, uiState.artist, uiState.url) {
        if (uiState.useSpotifyApi) {
            lyricsViewModel.fetchLyricsFromSpotify()
        } else {
            lyricsViewModel.fetchLyricsFromGenius()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            uiState.song,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            uiState.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    LGIconButton(
                        tooltip = stringResource(id = R.string.go_back),
                        onClick = goBack
                    ) {
                        Icon(
                            modifier = Modifier.mirror(),
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(
                                id = R.string.go_back
                            )
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .then(
                    if (!uiState.isLoading && uiState.lyrics.isNotEmpty()) Modifier.verticalScroll(
                        rememberScrollState()
                    ) else Modifier
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                if (uiState.error != null) {
                    Text(
                        uiState.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                } else if (uiState.lyrics.isEmpty()) {
                    Instrumental()
                } else {

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            Modifier.padding(
                                top = 12.dp,
                                start = 12.dp,
                                end = 12.dp,
                                bottom = 32.dp
                            )
                        ) {
                            SelectionContainer {
                                TextField(
                                    value = uiState.lyrics,
                                    onValueChange = {},
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    readOnly = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun Instrumental() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.MusicNote,
            contentDescription = "Music Note",
            modifier = Modifier.size(50.dp)
        )
        Text(
            stringResource(id = R.string.instrumental),
            fontStyle = FontStyle.Italic
        )
    }
}