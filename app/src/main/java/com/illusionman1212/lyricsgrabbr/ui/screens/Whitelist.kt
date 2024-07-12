package com.illusionman1212.lyricsgrabbr.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.illusionman1212.lyricsgrabbr.R
import com.illusionman1212.lyricsgrabbr.ui.components.LGIconButton
import com.illusionman1212.lyricsgrabbr.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistPage(goBack: () -> Unit, viewModel: SettingsViewModel) {
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.whitelist))
            },
            navigationIcon = {
                LGIconButton(tooltip = stringResource(id = R.string.go_back), onClick = goBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(
                        id = R.string.go_back
                    ))
                }
            }
        )
    }) { padding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        Box(Modifier.padding(padding)) {
            LazyColumn {
                for ((packageName, appInfo) in uiState.applications) {
                    item {
                        var isWhitelisted by remember { mutableStateOf(appInfo.isWhitelisted) }

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clickable(
                                    role = Role.Checkbox,
                                    onClick = {
                                        if (isWhitelisted) {
                                            viewModel.removeFromWhitelist(packageName)
                                        } else {
                                            viewModel.addToWhitelist(packageName)
                                        }
                                        isWhitelisted = !isWhitelisted
                                    }
                                )) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(modifier = Modifier.size(32.dp), model = appInfo.icon, contentDescription = null)
                                Text(appInfo.displayName, Modifier.weight(1f))
                                Checkbox(
                                    checked = isWhitelisted,
                                    onCheckedChange = null,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}