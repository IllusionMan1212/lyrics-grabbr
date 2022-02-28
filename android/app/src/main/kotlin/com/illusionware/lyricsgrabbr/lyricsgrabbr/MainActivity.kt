package com.illusionware.lyricsgrabbr.lyricsgrabbr

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel


class MainActivity: FlutterActivity() {
//    private var streamHandler : NotificationStreamHandler? = null
    private var listenerIntent : Intent? = null

    companion object {
        private const val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
        private const val EVENT_CHANNEL_NAME = "notifications.eventChannel"
        private const val METHOD_CHANNEL_NAME = "notifications/method"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listenerIntent = Intent(applicationContext, NotificationListener::class.java)

        val methodChannel = MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, METHOD_CHANNEL_NAME)
        methodChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "plugin.hasPermission" -> {
                    result.success(isPermissionGranted())
                }
                "plugin.openPermissionSettings" -> {
                    result.success(openPermissionSettings())
                }
                "plugin.startService" -> {
                    result.success(startService())
                }
                "plugin.stopService" -> {
                    result.success(stopService())
                }
                else -> result.notImplemented()
            }
        }

//        val eventChannel = EventChannel(flutterEngine!!.dartExecutor.binaryMessenger, EVENT_CHANNEL_NAME)
//        streamHandler = NotificationStreamHandler(context)
//        eventChannel.setStreamHandler(streamHandler)
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

    private fun startService(): Boolean {
        listenerIntent?.action = NotificationListener.START_FOREGROUND_SERVICE_ACTION
        ContextCompat.startForegroundService(applicationContext, listenerIntent!!)
        Log.d("startService", "starting")

        return true
    }

    private fun stopService(): Boolean {
        listenerIntent?.action = NotificationListener.STOP_FOREGROUND_SERVICE_ACTION
        ContextCompat.startForegroundService(applicationContext, listenerIntent!!)
        Log.d("stopService", "stopping")
        //applicationContext.stopService(listenerIntent!!)

        return true
    }
}
