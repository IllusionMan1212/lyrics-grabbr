package com.illusionware.lyricsgrabbr.lyricsgrabbr

import android.content.Intent
import android.text.TextUtils
import androidx.core.app.NotificationManagerCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    companion object {
        private const val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
        private const val EVENT_CHANNEL_NAME = "notifications.eventChannel"
        private const val METHOD_CHANNEL_NAME = "notifications/method"
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, METHOD_CHANNEL_NAME)
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

        val eventChannel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENT_CHANNEL_NAME)
        val streamHandler = NotificationStreamHandler(context)
        eventChannel.setStreamHandler(streamHandler)
    }

    private fun isPermissionGranted(): Boolean {
        val packageName = context.packageName
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
