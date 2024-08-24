package com.illusionman1212.lyricsgrabbr.ui.screens

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.illusionman1212.lyricsgrabbr.NotificationEvent
import com.illusionman1212.lyricsgrabbr.R
import com.illusionman1212.lyricsgrabbr.ui.components.LGAlertDialog
import com.illusionman1212.lyricsgrabbr.ui.components.LGIconButton
import com.illusionman1212.lyricsgrabbr.viewmodels.HomeViewModel
import com.illusionman1212.lyricsgrabbr.viewmodels.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import coil.compose.AsyncImage
import com.illusionman1212.lyricsgrabbr.ui.components.forwardingPainter
import com.illusionman1212.lyricsgrabbr.utils.annotatedStringResource
import com.illusionman1212.lyricsgrabbr.viewmodels.LyricsViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    navigateToSettings: () -> Unit,
    navigateToLyrics: () -> Unit,
    goBack: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory,
    ),
    lyricsViewModel: LyricsViewModel,
    lastNotification: MutableStateFlow<NotificationEvent?>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var artistUri by remember { mutableStateOf(Uri.EMPTY) }
    var songUri by remember { mutableStateOf(Uri.EMPTY) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var listeningPermission = homeViewModel.getListeningPermission()
    var notificationPermission by remember { mutableStateOf(
        ActivityCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PERMISSION_GRANTED
    ) }
    var shouldShowNotificationPermissionRationale by remember { mutableStateOf(
        ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity,
            POST_NOTIFICATIONS
        )
    )}

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermission = isGranted

        // we hide the rationale after launching once because launching again doesn't seem work
        shouldShowNotificationPermissionRationale = false
    }

    val song = lastNotification.collectAsStateWithLifecycle().value
    val uiState = homeViewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        if (!shouldShowNotificationPermissionRationale)
            launcher.launch(POST_NOTIFICATIONS)
    }

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                listeningPermission = homeViewModel.getListeningPermission()
            }
            else -> {}
        }
    }

    LaunchedEffect(song) {
        if (song != null && song.title != uiState.notification?.title && song.artist != uiState.notification?.artist) {
            homeViewModel.setSong(song)

            homeViewModel.makeRequestToGenius(context, song.title, song.artist)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    LGIconButton(
                        tooltip = stringResource(id = R.string.settings),
                        onClick = navigateToSettings,
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(id = R.string.settings)
                        )
                    }
                }
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(top = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!notificationPermission && shouldShowNotificationPermissionRationale) {
                LGAlertDialog(
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = false,
                    ),
                    onDismiss = goBack,
                    title = stringResource(id = R.string.notification_post_permission),
                    buttons = {
                        TextButton(
                            onClick = {
                                launcher.launch(POST_NOTIFICATIONS)
                            }
                        ) {
                            Text(stringResource(id = R.string.grant_permission))
                        }
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.notification_post_permission_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
            if (!listeningPermission && notificationPermission) {
                LGAlertDialog(
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = false,
                    ),
                    onDismiss = goBack,
                    title = stringResource(id = R.string.notification_listen_permission),
                    buttons = {
                        TextButton(
                            onClick = {
                                context.startActivity(
                                    android.content.Intent(
                                        android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(id = R.string.open_settings))
                        }
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.notification_listen_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (song == null) {
                    Column(
                        Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text( text = stringResource(id = R.string.no_supported_player), style = MaterialTheme.typography.headlineSmall )
                        if (!notificationPermission) {
                            Text(
                                text = stringResource(id = R.string.notification_post_permission_not_granted).uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                } else {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = song.title,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = song.artist,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator()
                            } else {
                                if (uiState.error != null) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = uiState.error,
                                            style = MaterialTheme.typography.titleLarge,
                                            textAlign = TextAlign.Center,
                                            fontStyle = FontStyle.Italic,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        FilledTonalButton(
                                            onClick = {
                                                homeViewModel.makeRequestToGenius(context, song.title, song.artist)
                                            }
                                        ) {
                                            Text(stringResource(id = R.string.retry))
                                        }
                                    }
                                } else {
                                    Results(
                                        uiState.results,
                                        showBottomSheet = { artistUrl, songUrl ->
                                            showBottomSheet = !(artistUrl == Uri.EMPTY && songUrl == Uri.EMPTY)
                                            artistUri = artistUrl
                                            songUri = songUrl
                                        },
                                        navigateToLyrics = navigateToLyrics,
                                        setLyricsTitle = { id, title, subtitle, url ->
                                            lyricsViewModel.onSongChange(id, title, subtitle, url)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                Column(
                    Modifier.height(36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val linkColor = MaterialTheme.colorScheme.surfaceTint
                    val text = annotatedStringResource(id = R.string.powered_by) {
                        when (it.key) {
                            "href" -> SpanStyle(color = linkColor)
                            else -> null
                        }
                    }

                    val textColor = LocalContentColor.current
                    val uriHandler = LocalUriHandler.current

                    ClickableText(
                        text,
                        onClick = { offset ->
                            text.getStringAnnotations(tag = "href", start = offset, end = offset)
                                .firstOrNull()
                                ?.let {
                                    uriHandler.openUri(it.item)
                                }
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(color = textColor, fontStyle = FontStyle.Italic),
                    )
                }
            }
        }

        if (showBottomSheet) {
            LongClickBottomSheet(
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showBottomSheet = false
                    }
                },
                sheetState = sheetState,
                artistUri = artistUri,
                songUri = songUri,
            )
        }
    }
}

@Composable
fun Results(
    results: List<SearchResult>,
    navigateToLyrics: () -> Unit,
    setLyricsTitle: (id: String, title: String, subtitle: String, url: Uri) -> Unit,
    showBottomSheet: (artistUrl: Uri, songUrl: Uri) -> Unit,
) {
    if (results.isEmpty()) {
        Text(stringResource(id = R.string.no_results), style = MaterialTheme.typography.titleLarge)
    } else {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(id = R.string.found_results, results.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
            )
            LazyColumn {
                items(results) {
                    ResultListItem(
                        it,
                        onClick = {
                            setLyricsTitle(it.id, it.title, it.artist, it.url)
                            navigateToLyrics()
                        },
                        onLongClick = {
                            showBottomSheet(it.artistUrl, it.url)
                        }
                    )

                    if (it != results.last())
                        HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongClickBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    artistUri: Uri,
    songUri: Uri,
) {
    val uriHandler = LocalUriHandler.current

    ModalBottomSheet(
        dragHandle = {},
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column {
            if (artistUri != Uri.EMPTY) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri(artistUri.toString()) }
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.LibraryMusic, contentDescription = "Music Library")
                    Text(stringResource(id = R.string.open_artist_page), fontWeight = FontWeight.Bold)
                }
            }
            if (songUri != Uri.EMPTY) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri(songUri.toString()) }
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = "Music Note")
                    Text(stringResource(id = R.string.open_song_page), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultListItem(
    item: SearchResult,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onLongClick()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            )
    ) {
        val color = LocalContentColor.current
        val density = LocalDensity.current

        AsyncImage(
            item.thumbnail,
            error = forwardingPainter(
                rememberVectorPainter(image = Icons.Filled.BrokenImage),
                colorFilter = ColorFilter.tint(color),
                size = with(density) { DpSize(40.dp, 40.dp).toSize() },
            ),
            fallback = rememberVectorPainter(Icons.Filled.Image),
            contentDescription = "Thumbnail for ${item.title}",
            modifier = Modifier.size(75.dp),
            contentScale = ContentScale.Crop,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                item.title,
                fontSize = TextUnit(18F, TextUnitType.Sp),
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.artist,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ResultListItemPreview() {
    ResultListItem(
        item = SearchResult(
            "Test",
            "Artist",
            id = "1",
            url = Uri.EMPTY,
            artistUrl = Uri.EMPTY,
            thumbnail = Uri.EMPTY
        ),
        onClick = {},
        onLongClick = {},
    )
}
