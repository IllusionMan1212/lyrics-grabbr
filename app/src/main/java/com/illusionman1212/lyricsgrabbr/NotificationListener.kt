package com.illusionman1212.lyricsgrabbr

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class NotificationListener: NotificationListenerService() {
    private var notificationManager : NotificationManagerCompat? = null

    override fun onCreate() {
        super.onCreate()

        initNotificationChannels(this)
        notificationManager = NotificationManagerCompat.from(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                START_FOREGROUND_SERVICE_ACTION -> {
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
        sendNotificationBroadcast(sbn, applicationContext)
    }

    private fun getStopServiceNotificationAction(): PendingIntent {
        val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun startForegroundService() {
        startForeground(NOTIFICATION_ID, getInitialNotification())
    }

    private fun stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
    }

    private fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
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
            sendNotificationBroadcast(sbn, context)
        }
    }

    private fun getInitialNotification(): Notification {
        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(resources.getString(R.string.service_running))
            .setContentText(resources.getString(R.string.service_notif_desc_init))
            .setSmallIcon(R.drawable.ic_notification)
            .setShowWhen(false)
            .addAction(R.drawable.ic_notification, resources.getString(R.string.stop_service), getStopServiceNotificationAction())
            .build()

        notification.flags = Notification.FLAG_FOREGROUND_SERVICE

        return notification
    }

    private fun sendNotificationBroadcast(sbn: StatusBarNotification, context: Context) {
        if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
            return
        }

        val mediaSessionToken = BundleCompat.getParcelable(sbn.notification.extras, Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
            ?: return

        val mediaCtrl = MediaController(context, mediaSessionToken)
        val playbackState = mediaCtrl.playbackState?.state
        val albumArt = mediaCtrl.metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)

        // Retrieve extra object from notification to extract payload.
        val extras = sbn.notification.extras
        val packageArtist = extras?.getCharSequence(Notification.EXTRA_TEXT).toString()
        val packageSongTitle = extras?.getCharSequence("android.title").toString()
        // Pass data from one activity to another.
        val intent = Intent(NOTIFICATION_INTENT)
        intent.putExtra(NOTIFICATION_PACKAGE_NAME, sbn.packageName)
        intent.putExtra(NOTIFICATION_PACKAGE_ARTIST, packageArtist)
        intent.putExtra(NOTIFICATION_PACKAGE_SONG_TITLE, packageSongTitle)
        intent.putExtra(NOTIFICATION_PACKAGE_PLAYBACK_STATE, playbackState)

        if (playbackState == PlaybackState.STATE_PAUSED) {
            notificationManager?.notify(NOTIFICATION_ID, getInitialNotification())
        } else {
            val notificationIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(resources.getString(R.string.now_playing, "[${packageSongTitle}]", "[${packageArtist}]"))
                .setContentText(resources.getString(R.string.service_notif_desc_playing))
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(albumArt)
                .setShowWhen(false)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_notification, resources.getString(R.string.stop_service), getStopServiceNotificationAction())
                .build()

            notification.flags = Notification.FLAG_FOREGROUND_SERVICE
            notificationManager?.notify(NOTIFICATION_ID, notification)
        }

        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    companion object {
        const val NOTIFICATION_INTENT = "notification_event"
        const val NOTIFICATION_PACKAGE_NAME = "package_name"
        const val NOTIFICATION_PACKAGE_ARTIST = "package_artist"
        const val NOTIFICATION_PACKAGE_SONG_TITLE = "package_song_title"
        const val NOTIFICATION_PACKAGE_PLAYBACK_STATE = "package_playback_state"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "Lyrics"
        const val START_FOREGROUND_SERVICE_ACTION = "START_SERVICE"
    }
}
