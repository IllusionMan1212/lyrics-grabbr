package com.illusionware.lyricsgrabbr.lyricsgrabbr

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


/**
 * Notification listening service. Intercepts notifications if permission is given to do so.
 */
class NotificationListener : NotificationListenerService() {
    private var notificationManager : NotificationManagerCompat? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("onCreate Service", "creating service")

        initNotificationChannels(this)
        notificationManager = NotificationManagerCompat.from(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                START_FOREGROUND_SERVICE_ACTION -> {
                    Log.d("START", "starting foreground service")
                    startForegroundService()
                    getCurrentNotifications(applicationContext)
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onListenerConnected() {
        getCurrentNotifications(applicationContext)
    }

    override fun onListenerDisconnected() {
        stopForegroundService()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == SPOTIFY_PACKAGE) {
            sendNotificationBroadcast(sbn, applicationContext)
        }
    }

    private fun getStopServiceNotificationAction(): PendingIntent {
        val intent = Intent(MainActivity.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        return pendingIntent
    }

    private fun startForegroundService() {
        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Service Running")
            .setContentText("Play some songs on spotify to get lyrics")
            .setSmallIcon(R.drawable.ic_notification)
            .setShowWhen(false)
            .addAction(R.drawable.ic_notification, "Stop Service", getStopServiceNotificationAction())
            .build()

        notification.flags = Notification.FLAG_FOREGROUND_SERVICE
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }

    private fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Lyrics",
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }

    private fun getCurrentNotifications(context: Context) {
        val sbns = activeNotifications
        for (sbn in sbns) {
            if (sbn.packageName == SPOTIFY_PACKAGE) {
                sendNotificationBroadcast(sbn, context)
            }
        }
    }

    private fun sendNotificationBroadcast(sbn: StatusBarNotification, context: Context) {
        // get the media session stuff
        val mediaSessionToken = sbn.notification.extras.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)
        val mediaCtrl = mediaSessionToken?.let { MediaController(context, it) }
        val duration = mediaCtrl?.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.div(1000)
        val playbackState = mediaCtrl?.playbackState?.state
        val albumArt = mediaCtrl?.metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)

        // Retrieve package name to set as title.
        val packageName = sbn.packageName
        // Retrieve extra object from notification to extract payload.
        val extras = sbn.notification.extras
        val packageMessage = extras?.getCharSequence(Notification.EXTRA_TEXT).toString()
        val packageText = extras?.getCharSequence("android.title").toString()
        // Pass data from one activity to another.
        val intent = Intent(NOTIFICATION_INTENT)
        intent.putExtra(NOTIFICATION_PACKAGE_NAME, packageName)
        intent.putExtra(NOTIFICATION_PACKAGE_MESSAGE, packageMessage)
        intent.putExtra(NOTIFICATION_PACKAGE_TEXT, packageText)
        intent.putExtra(NOTIFICATION_PACKAGE_DURATION, duration)
        intent.putExtra(NOTIFICATION_PACKAGE_PLAYBACK_STATE, playbackState)

        if (playbackState == PlaybackState.STATE_PAUSED) {
            val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Service Running")
                .setContentText("Play some songs on spotify to get lyrics")
                .setSmallIcon(R.drawable.ic_notification)
                .setShowWhen(false)
                .addAction(R.drawable.ic_notification, "Stop Service", getStopServiceNotificationAction())
                .build()

            notification.flags = Notification.FLAG_FOREGROUND_SERVICE
            notificationManager?.notify(NOTIFICATION_ID, notification)
        } else {
            val notificationIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Playing [${packageText}] By [${packageMessage}]")
                .setContentText("Tap to get lyrics")
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(albumArt)
                .setShowWhen(false)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_notification, "Stop Service", getStopServiceNotificationAction())
                .build()

            notification.flags = Notification.FLAG_FOREGROUND_SERVICE
            notificationManager?.notify(NOTIFICATION_ID, notification)
        }

        sendBroadcast(intent)
    }

    companion object {
        const val NOTIFICATION_INTENT = "notification_event"
        const val NOTIFICATION_PACKAGE_NAME = "package_name"
        const val NOTIFICATION_PACKAGE_MESSAGE = "package_message"
        const val NOTIFICATION_PACKAGE_TEXT = "package_text"
        const val NOTIFICATION_PACKAGE_PLAYBACK_STATE = "package_playback_state"
        const val NOTIFICATION_PACKAGE_DURATION = "package_duration"
        const val NOTIFICATION_ID = 12251999
        const val NOTIFICATION_CHANNEL_ID = "Lyrics"
        const val SPOTIFY_PACKAGE = "com.spotify.music"
        const val START_FOREGROUND_SERVICE_ACTION = "START_SERVICE"
    }
}
