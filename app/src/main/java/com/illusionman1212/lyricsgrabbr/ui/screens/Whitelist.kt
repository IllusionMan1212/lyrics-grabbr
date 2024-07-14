package com.illusionman1212.lyricsgrabbr.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.illusionman1212.lyricsgrabbr.R
import com.illusionman1212.lyricsgrabbr.ui.components.LGIconButton
import com.illusionman1212.lyricsgrabbr.ui.theme.Typography
import com.illusionman1212.lyricsgrabbr.viewmodels.AppInfo
import com.illusionman1212.lyricsgrabbr.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistPage(goBack: () -> Unit, viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        TopAppBar(
            title = {
                TextField(
                    value = uiState.whitelistSearchQuery,
                    onValueChange = {
                        viewModel.filterPackages(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(text = stringResource(id = R.string.search), style = Typography.titleLarge)
                    },
                    trailingIcon = {
                        LGIconButton(tooltip = stringResource(id = R.string.clear), onClick = { viewModel.clearWhitelistSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(id = R.string.clear))
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
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
        val context = LocalContext.current

        val refreshState = rememberPullToRefreshState()
        var isRefreshing by remember { mutableStateOf(false) }

        if (uiState.applications.isNotEmpty()) {
            isRefreshing = false
        }

        PullToRefreshBox(
            state = refreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refreshPackages(context)
            },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.applications.any { it.value.isWhitelisted }) {
                    for ((packageName, appInfo) in uiState.applications.filter { it.value.isWhitelisted }) {
                        item {
                            Application(packageName, appInfo, viewModel)
                        }
                    }
                    item {
                        HorizontalDivider(
                            Modifier
                                .width(84.dp)
                                .padding(vertical = 12.dp))
                    }
                }
                for ((packageName, appInfo) in uiState.applications.filter { !it.value.isWhitelisted }) {
                    item {
                        Application(packageName, appInfo, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun Application(packageName: String, app: AppInfo, viewModel: SettingsViewModel) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Checkbox,
                onClick = {
                    if (app.isWhitelisted) {
                        viewModel.removeFromWhitelist(packageName)
                    } else {
                        viewModel.addToWhitelist(packageName)
                    }
                }
            )) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(modifier = Modifier.size(32.dp), model = app.icon, contentDescription = null)
            Text(app.displayName, Modifier.weight(1f))
            Checkbox(
                checked = app.isWhitelisted,
                onCheckedChange = null,
            )
        }
    }
}