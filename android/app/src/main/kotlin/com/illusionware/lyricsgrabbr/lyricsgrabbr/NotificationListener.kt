package com.illusionware.lyricsgrabbr.lyricsgrabbr

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONException
import org.json.JSONObject
import android.os.Bundle

/**
 * Notification listening service. Intercepts notifications if permission is given to do so.
 */
class NotificationListener : NotificationListenerService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        getCurrentNotifications()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onListenerConnected() {
        getCurrentNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        sendNotificationBroadcast(sbn)
    }

    private fun getCurrentNotifications() {
        val sbns = activeNotifications
        for (sbn in sbns) {
            // TODO: remove this check and figure out a way to send a fucking list instead
            if (sbn.packageName == "com.spotify.music") {
                sendNotificationBroadcast(sbn)
            }
        }
    }

    private fun sendNotificationBroadcast(sbn: StatusBarNotification) {
        // TODO: get specific media player notification data
        // such as onPlay and onNextSong or whatever tf

        // Retrieve package name to set as title.
        val packageName = sbn.packageName
        // Retrieve extra object from notification to extract payload.
        val extras = sbn.notification.extras
        val packageMessage = extras?.getCharSequence(Notification.EXTRA_TEXT).toString()
        val packageText = extras?.getCharSequence("android.title").toString()
        val packageExtra = convertBundleToJsonString(sbn.notification.extras)
        // Pass data from one activity to another.
        val intent = Intent(NOTIFICATION_INTENT)
        intent.putExtra(NOTIFICATION_PACKAGE_NAME, packageName)
        intent.putExtra(NOTIFICATION_PACKAGE_MESSAGE, packageMessage)
        intent.putExtra(NOTIFICATION_PACKAGE_TEXT, packageText)
        intent.putExtra(NOTIFICATION_PACKAGE_EXTRA, packageExtra)
        sendBroadcast(intent)
    }

    companion object {
        const val NOTIFICATION_INTENT = "notification_event"
        const val NOTIFICATION_PACKAGE_NAME = "package_name"
        const val NOTIFICATION_PACKAGE_MESSAGE = "package_message"
        const val NOTIFICATION_PACKAGE_TEXT = "package_text"
        const val NOTIFICATION_PACKAGE_EXTRA = "package_extra"
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
