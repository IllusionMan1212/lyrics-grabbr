package com.illusionman1212.lyricsgrabbr

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel


class MainActivity: FlutterActivity() {
    private var streamHandler : NotificationStreamHandler? = null

    companion object {
        const val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
        private const val EVENT_CHANNEL_NAME = "notifications.eventChannel"
        private const val METHOD_CHANNEL_NAME = "notifications/method"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val methodChannel = MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, METHOD_CHANNEL_NAME)
        methodChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "plugin.hasPermission" -> {
                    result.success(isPermissionGranted())
                }
                "plugin.openPermissionSettings" -> {
                    result.success(openPermissionSettings())
                }
                else -> result.notImplemented()
            }
        }

        val eventChannel = EventChannel(flutterEngine!!.dartExecutor.binaryMessenger, EVENT_CHANNEL_NAME)
        streamHandler = NotificationStreamHandler(context)
        eventChannel.setStreamHandler(streamHandler)
    }

    override fun onDestroy() {
        if (streamHandler?.receiver != null) {
            context.unregisterReceiver(streamHandler?.receiver)
        }
        super.onDestroy()
    }

    private fun isPermissionGranted(): Boolean {
        val packageName = packageName
        val pkgs = NotificationManagerCompat.getEnabledListenerPackages(context)
        for (name in pkgs) {
            if (TextUtils.equals(packageName, name)) {
                return true
            }
        }

        return false
    }

    private fun openPermissionSettings(): Boolean {
        context.startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }
}
