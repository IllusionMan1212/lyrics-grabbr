package com.illusionware.lyricsgrabbr.lyricsgrabbr

import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.EventChannel.EventSink
import android.content.*
import android.util.Log
import androidx.core.content.ContextCompat

class NotificationStreamHandler(private val context: Context) : StreamHandler {
  private var eventSink: EventSink? = null
  var receiver: NotificationReceiver? = null
  private var listenerIntent : Intent? = null

  // Called whenever the event channel is subscribed to in Flutter
  override fun onListen(o: Any?, eventSink: EventSink?) {
    this.eventSink = eventSink

    listenerIntent = Intent(context, NotificationListener::class.java)
    listenerIntent?.action = NotificationListener.START_FOREGROUND_SERVICE_ACTION

    receiver = NotificationReceiver()
    val intentFilter = IntentFilter()
    intentFilter.addAction(NotificationListener.NOTIFICATION_INTENT)
    context.registerReceiver(receiver, intentFilter)

    ContextCompat.startForegroundService(context, listenerIntent!!)
  }

  // Called whenever the event channel subscription is cancelled in Flutter
  override fun onCancel(o: Any?) {
    eventSink = null
    Log.d("onCancel", "subscription cancelled, stopping service")

    context.unregisterReceiver(receiver)
  }

  inner class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val packageName = intent.getStringExtra(NotificationListener.NOTIFICATION_PACKAGE_NAME)
      val packageMessage = intent.getStringExtra(NotificationListener.NOTIFICATION_PACKAGE_MESSAGE)
      val packageText = intent.getStringExtra(NotificationListener.NOTIFICATION_PACKAGE_TEXT)
      val packagePlaybackState = intent.getIntExtra(NotificationListener.NOTIFICATION_PACKAGE_PLAYBACK_STATE, -1)
      val packageDuration = intent.getLongExtra(NotificationListener.NOTIFICATION_PACKAGE_DURATION, -1)
      val map = HashMap<String, Any>()
      map["packageName"] = packageName!!
      map["packageMessage"] = packageMessage!!
      map["packageText"] = packageText!!
      map["packagePlaybackState"] = packagePlaybackState
      map["packageDuration"] = packageDuration
      eventSink?.success(map)
    }
  }
}
