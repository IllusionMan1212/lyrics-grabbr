package com.illusionware.lyricsgrabbr.lyricsgrabbr

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONException
import org.json.JSONObject
import android.os.Bundle
import java.io.File
import java.io.FileOutputStream

/**
 * Notification listening service. Intercepts notifications if permission is given to do so.
 */
class NotificationListener : NotificationListenerService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        getCurrentNotifications(applicationContext)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onListenerConnected() {
        getCurrentNotifications(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        sendNotificationBroadcast(sbn, applicationContext)
    }

    private fun getCurrentNotifications(context: Context) {
        val sbns = activeNotifications
        for (sbn in sbns) {
            // TODO: remove this check and figure out a way to send a fucking list instead
            if (sbn.packageName == "com.spotify.music") {
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

        var albumArtPath = ""

        if (sbn.packageName == "com.spotify.music") {
            val albumArt = mediaCtrl?.metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            val tempArtFile = File(cacheDir, "albumart_temp.png")
            tempArtFile.createNewFile()
            val fileOutputStream = FileOutputStream(tempArtFile)
            albumArt?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            albumArtPath = tempArtFile.absolutePath
        }

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
        intent.putExtra(NOTIFICATION_PACKAGE_ART, albumArtPath)
        intent.putExtra(NOTIFICATION_PACKAGE_DURATION, duration)
        intent.putExtra(NOTIFICATION_PACKAGE_PLAYBACK_STATE, playbackState)
        sendBroadcast(intent)
    }

    companion object {
        const val NOTIFICATION_INTENT = "notification_event"
        const val NOTIFICATION_PACKAGE_NAME = "package_name"
        const val NOTIFICATION_PACKAGE_MESSAGE = "package_message"
        const val NOTIFICATION_PACKAGE_TEXT = "package_text"
        const val NOTIFICATION_PACKAGE_EXTRA = "package_extra"
        const val NOTIFICATION_PACKAGE_ART = "package_art"
        const val NOTIFICATION_PACKAGE_PLAYBACK_STATE = "package_playback_state"
        const val NOTIFICATION_PACKAGE_DURATION = "package_duration"
    }

    private fun convertBundleToJsonString(extra: Bundle): String {
        val json = JSONObject()
        val keys = extra.keySet()
        for (key in keys) {
            try {
                json.put(key, JSONObject.wrap(extra.get(key)))
            } catch (e: JSONException) {
                //Handle exception here
            }
        }
        return json.toString()
    }
}
