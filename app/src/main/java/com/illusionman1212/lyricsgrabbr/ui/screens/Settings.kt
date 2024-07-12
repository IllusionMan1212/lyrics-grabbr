package com.illusionman1212.lyricsgrabbr.ui.screens

import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.illusionman1212.lyricsgrabbr.R
import com.illusionman1212.lyricsgrabbr.ui.components.LGAlertDialog
import com.illusionman1212.lyricsgrabbr.ui.components.LGRadioButton
import com.illusionman1212.lyricsgrabbr.ui.theme.ColorSecondaryLight
import com.illusionman1212.lyricsgrabbr.data.Theme
import com.illusionman1212.lyricsgrabbr.ui.components.LGIconButton
import com.illusionman1212.lyricsgrabbr.utils.getLangPreferenceDropdownEntries
import com.illusionman1212.lyricsgrabbr.viewmodels.SettingsState
import com.illusionman1212.lyricsgrabbr.viewmodels.SettingsViewModel

private val Themes = hashMapOf(
    Theme.LIGHT.ordinal to R.string.theme_light,
    Theme.DARK.ordinal to R.string.theme_dark,
    Theme.SYSTEM.ordinal to R.string.theme_system
)

@Composable
fun SettingsPage(
    goBack: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .fillMaxWidth(),
            ) {
                LGIconButton(tooltip = stringResource(id = R.string.go_back), onClick = goBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(
                            id = R.string.go_back
                        ) )
                }
                Text(text = stringResource(id = R.string.settings), style = MaterialTheme.typography.titleLarge)
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
        }
    }
}

@Composable
fun Appearance(viewModel: SettingsViewModel, uiState: SettingsState) {
    var themeOpenDialog by remember { mutableStateOf(false) }
    var languageOpenDialog by remember { mutableStateOf(false) }

    SettingGroup(title = stringResource(id = R.string.appearance)) {
        DialogSetting(
            title = stringResource(id = R.string.theme),
            value = Themes[uiState.appTheme]?.let { stringResource(it) } ?: "None",
            icon = Icons.Outlined.Palette,
            onClick = { themeOpenDialog = true }
        )
        DialogSetting(
            title = stringResource(id = R.string.language),
            value = AppCompatDelegate.getApplicationLocales()[0]?.let {
                AppCompatDelegate.getApplicationLocales()[0]?.getDisplayLanguage(
                    it
                )
            }
                ?: stringResource(id = R.string._default),
            icon = Icons.Outlined.Translate,
            onClick = { languageOpenDialog = true }
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
    if (languageOpenDialog) {
        LanguageDialog(
            onDismiss = { languageOpenDialog = false },
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
fun LanguageDialog(
    onDismiss: () -> Unit,
) {
    val langs = LocalContext.current.getLangPreferenceDropdownEntries()

    LGAlertDialog(
        onDismiss = onDismiss,
        title = stringResource(id = R.string.language),
        buttons = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyColumn {
            item {
                LGRadioButton(
                    text = stringResource(id = R.string._default),
                    onClick = {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                    },
                    selected = AppCompatDelegate.getApplicationLocales().isEmpty,
                )
            }
            for ((key, value) in langs) {
                item {
                    LGRadioButton(
                        text = key,
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(value))
                        },
                        selected = AppCompatDelegate.getApplicationLocales()[0]?.language == value,
                    )
                }
            }
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
private fun Header(title: String) {
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