package com.illusionman1212.lyricsgrabbr

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.illusionman1212.lyricsgrabbr.data.Theme
import com.illusionman1212.lyricsgrabbr.ui.screens.HomePage
import com.illusionman1212.lyricsgrabbr.ui.screens.LyricsPage
import com.illusionman1212.lyricsgrabbr.ui.theme.LyricsGrabbrTheme
import com.illusionman1212.lyricsgrabbr.ui.screens.SettingsPage
import com.illusionman1212.lyricsgrabbr.viewmodels.LyricsViewModel
import com.illusionman1212.lyricsgrabbr.viewmodels.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

data class NotificationEvent(
    val packageName: String,
    val artist: String,
    val title: String,
    val playbackState: Int,
)

class MainActivity : AppCompatActivity() {
    private var lastNotification: MutableStateFlow<NotificationEvent?> = MutableStateFlow(null)
    private var isReceiverRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory
            )
            val lyricsViewModel: LyricsViewModel = viewModel(
                factory = LyricsViewModel.Factory
            )

            val appTheme = remember { mutableIntStateOf(Theme.SYSTEM.ordinal) }
            val navController = rememberNavController()

            LaunchedEffect(settingsViewModel.uiState) {
                settingsViewModel.uiState.collect { newState ->
                    if (appTheme.intValue != newState.appTheme) {
                        appTheme.intValue = newState.appTheme
                    }
                }
            }

            runBlocking {
                appTheme.intValue = settingsViewModel.getAppTheme()
            }

            val darkTheme = when (appTheme.intValue) {
                Theme.LIGHT.ordinal -> false
                Theme.DARK.ordinal -> true
                else -> isSystemInDarkTheme()
            }

            LyricsGrabbrTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Home.route,
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { 1000 },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { -1000 },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                    ) {
                        composable(Routes.Home.route) {
                            HomePage(
                                navigateToSettings = {
                                    navController.navigate(Routes.Settings.route)
                                },
                                navigateToLyrics = {
                                    navController.navigate(Routes.Lyrics.route)
                                },
                                lyricsViewModel = lyricsViewModel,
                                goBack = { finish() },
                                lastNotification = lastNotification,
                            )
                        }
                        composable(Routes.Lyrics.route) {
                            LyricsPage(
                                goBack = {
                                    navController.popBackStack()
                                },
                                lyricsViewModel = lyricsViewModel,
                            )
                        }
                        composable(Routes.Settings.route) {
                            SettingsPage(
                                goBack = {
                                    navController.popBackStack()
                                },
                                viewModel = settingsViewModel,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        startListening()
    }

    private fun isPermissionGranted(): Boolean {
        val packageName = packageName
        val pkgs = NotificationManagerCompat.getEnabledListenerPackages(applicationContext)

        return pkgs.contains(packageName)
    }

    private fun startListening() {
        if (!isPermissionGranted() || isReceiverRunning) {
            return
        }

        val listenerIntent = Intent(applicationContext, NotificationListener::class.java)
        listenerIntent.action = NotificationListener.START_FOREGROUND_SERVICE_ACTION

        val filter = IntentFilter()
        filter.addAction(NotificationListener.NOTIFICATION_INTENT)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(NotificationReceiver(), filter)
        ContextCompat.startForegroundService(applicationContext, listenerIntent)
        isReceiverRunning = true
    }

    inner class NotificationReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val artist = intent.getStringExtra(NotificationListener.NOTIFICATION_PACKAGE_ARTIST)
            val songTitle = intent.getStringExtra(NotificationListener.NOTIFICATION_PACKAGE_SONG_TITLE)
            val playbackState = intent.getIntExtra(NotificationListener.NOTIFICATION_PACKAGE_PLAYBACK_STATE, -1)
            val notifPackageName = intent.getStringExtra(NotificationListener.NOTIFICATION_PACKAGE_NAME)

            lastNotification.update { NotificationEvent(
                title = songTitle!!,
                artist = artist!!,
                playbackState = playbackState,
                packageName = notifPackageName!!,
            ) }
        }
    }
}