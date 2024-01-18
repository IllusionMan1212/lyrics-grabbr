package com.illusionman1212.lyricsgrabbr.ui.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.SpotifyException
import com.adamratzman.spotify.auth.pkce.startSpotifyClientPkceLoginActivity
import com.illusionman1212.lyricsgrabbr.LGApp
import com.illusionman1212.lyricsgrabbr.R
import com.illusionman1212.lyricsgrabbr.SpotifyPkceLoginActivityImpl
import com.illusionman1212.lyricsgrabbr.ui.components.LGAlertDialog
import com.illusionman1212.lyricsgrabbr.ui.components.LGRadioButton
import com.illusionman1212.lyricsgrabbr.ui.theme.ColorSecondaryLight
import com.illusionman1212.lyricsgrabbr.data.Theme
import com.illusionman1212.lyricsgrabbr.ui.components.LGIconButton
import com.illusionman1212.lyricsgrabbr.utils.annotatedStringResource
import com.illusionman1212.lyricsgrabbr.viewmodels.SettingsState
import com.illusionman1212.lyricsgrabbr.viewmodels.SettingsViewModel
import kotlinx.coroutines.runBlocking

private val Themes = hashMapOf(
    Theme.LIGHT.ordinal to R.string.theme_light,
    Theme.DARK.ordinal to R.string.theme_dark,
    Theme.SYSTEM.ordinal to R.string.theme_system
)

// TODO: this is here as a reference because I think I will need a guard in the future
fun <T> Activity.guardValidSpotifyApi(
    classBackTo: Class<out Activity>,
    alreadyTriedToReauthenticate: Boolean = false,
    block: suspend (api: SpotifyClientApi) -> T
): T? {
    return runBlocking {
        val app = (application as LGApp)

        try {
            val token = app.credentialStore.spotifyToken
                ?: throw SpotifyException.ReAuthenticationNeededException()
            val usesPkceAuth = token.refreshToken != null
            val api = (if (usesPkceAuth) app.credentialStore.getSpotifyClientPkceApi()
            else app.credentialStore.getSpotifyImplicitGrantApi())
                ?: throw SpotifyException.ReAuthenticationNeededException()

            block(api)
        } catch (e: SpotifyException) {
            e.printStackTrace()
            val api = app.credentialStore.getSpotifyClientPkceApi()!!
            if (!alreadyTriedToReauthenticate) {
                try {
                    api.refreshToken()
                    app.credentialStore.spotifyToken = api.token
                    block(api)
                } catch (e: SpotifyException.ReAuthenticationNeededException) {
                    e.printStackTrace()
                    return@runBlocking guardValidSpotifyApi(
                        classBackTo = classBackTo,
                        alreadyTriedToReauthenticate = true,
                        block = block
                    )
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    return@runBlocking guardValidSpotifyApi(
                        classBackTo = classBackTo,
                        alreadyTriedToReauthenticate = true,
                        block = block
                    )
                }
            } else {
                //pkceClassBackTo = classBackTo
                startSpotifyClientPkceLoginActivity(SpotifyPkceLoginActivityImpl::class.java)
                null
            }
        }
    }
}

@Composable
fun SettingsPage(
    goBack: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            // TODO: on RTL languages all newer(only?) android systems change the direction of the status bar
            //  including all its children, so we need to figure out how to position stuff to the right
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                ) {
                    LGIconButton(tooltip = stringResource(id = R.string.go_back), onClick = goBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(
                            id = R.string.go_back
                        ) )
                    }
                    Text(text = stringResource(id = R.string.settings), style = MaterialTheme.typography.titleLarge)
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Appearance(viewModel, uiState)
            Lyrics(viewModel, uiState)
        }
    }
}

@Composable
fun Appearance(viewModel: SettingsViewModel, uiState: SettingsState) {
    var themeOpenDialog by remember { mutableStateOf(false) }

    SettingGroup(title = stringResource(id = R.string.appearance)) {
        DialogSetting(
            title = stringResource(id = R.string.theme),
            value = Themes[uiState.appTheme]?.let { stringResource(it) } ?: "None",
            icon = Icons.Outlined.Palette,
            onClick = { themeOpenDialog = true }
        )
    }
    if (themeOpenDialog) {
        ThemeDialog(
            uiState = uiState,
            onDismiss = { themeOpenDialog = false },
            onThemeChange = {
                viewModel.setTheme(it)
            }
        )
    }
}

@Composable
fun ThemeDialog(
    uiState: SettingsState,
    onDismiss: () -> Unit,
    onThemeChange: (v: Theme) -> Unit
) {
    LGAlertDialog(
        onDismiss = onDismiss,
        title = stringResource(id = R.string.theme),
        buttons = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            LGRadioButton(
                text = stringResource(id = R.string.theme_light),
                onClick = {
                    onThemeChange(Theme.LIGHT)
                },
                selected = uiState.appTheme == Theme.LIGHT.ordinal,
            )
            LGRadioButton(
                text = stringResource(id = R.string.theme_dark),
                onClick = {
                    onThemeChange(Theme.DARK)
                },
                selected = uiState.appTheme == Theme.DARK.ordinal,
            )
            LGRadioButton(
                text = stringResource(id = R.string.theme_system),
                onClick = {
                    onThemeChange(Theme.SYSTEM)
                },
                selected = uiState.appTheme == Theme.SYSTEM.ordinal,
            )
        }
    }
}

@Composable
fun Lyrics(viewModel: SettingsViewModel, uiState: SettingsState) {
    val context = LocalContext.current
    val activity = context as Activity
    val application = activity.application as LGApp

    var isDialogOpen by remember { mutableStateOf(false) }
    var spotifyIdValue by remember { mutableStateOf("") }

    SettingGroup(stringResource(id = R.string.lyrics)) {
        ToggleSetting(
            title = stringResource(id = R.string.use_spotify_api),
            checked = uiState.isSpotifyAuthed,
            icon = painterResource(id = R.drawable.spotify),
            onClick = {
                if (uiState.isSpotifyAuthed) {
                    application.credentialStore.clear()
                    viewModel.updateSpotifyAuthStatus(false)
                } else {
                    isDialogOpen = true
                }
            },
        )
    }

    if (isDialogOpen) {
        LGAlertDialog(
            onDismiss = { isDialogOpen = false },
            title = stringResource(id = R.string.spotify_client_id),
            buttons = {
                TextButton(onClick = { isDialogOpen = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
                TextButton(onClick = {
                    SpotifyPkceLoginActivityImpl.CLIENT_ID = spotifyIdValue
                    SpotifyPkceLoginActivityImpl.settingsViewModel = viewModel
                    activity.startSpotifyClientPkceLoginActivity(SpotifyPkceLoginActivityImpl::class.java)
                    isDialogOpen = false
                }) {
                    Text(stringResource(id = R.string.confirm))
                }
            }
        ) {
            val linkColor = MaterialTheme.colorScheme.surfaceTint
            val spotifyDesc = annotatedStringResource(R.string.spotify_client_id_desc) {
                when (it.key) {
                    "href" -> SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
                    else -> null
                }
            }
            val textColor = LocalContentColor.current
            val uriHandler = LocalUriHandler.current

            ClickableText(
                spotifyDesc,
                onClick = { offset ->
                    spotifyDesc.getStringAnnotations(tag = "href", start = offset, end = offset)
                        .firstOrNull()
                        ?.let {
                            uriHandler.openUri(it.item)
                        }
                },
                style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
            )

            TextField(
                value = spotifyIdValue,
                onValueChange = { spotifyIdValue = it },
                placeholder = {
                    Text(stringResource(id = R.string.not_set))
                },
                visualTransformation = PasswordVisualTransformation(),
            )
        }
    }
}

@Composable
fun SettingGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Header(title)
        Spacer(modifier = Modifier.size(2.dp))
        content()
    }
}

@Composable
fun Header(title: String) {
    Row(Modifier.padding(start = 56.dp)) {
        Text(text = title, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun DialogSetting(
    title: String,
    value: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    singleLine : Boolean = false,
    enabled: Boolean = true
) {
    Box(
        Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.width(56.dp))
            } else {
                Box(modifier = Modifier.width(56.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    text = value,
                    color = ColorSecondaryLight,
                    fontSize = 14.sp,
                    maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }
    }
}

@Composable
fun ToggleSetting(
    title: String,
    icon: Painter? = null,
    checked: Boolean,
    onClick: (Boolean) -> Unit)
{
    Box(
        modifier = Modifier.clickable onClick@{
            onClick(!checked)
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(top = 8.dp, end = 24.dp, bottom = 8.dp),
        ) {
            if (icon != null) {
                Icon(painter = icon, contentDescription = null, Modifier.width(56.dp))
            } else {
                Box(modifier = Modifier.width(56.dp))
            }
            Text(text = title, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1.0f))
            Switch(
                checked = checked,
                onCheckedChange = null,
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}