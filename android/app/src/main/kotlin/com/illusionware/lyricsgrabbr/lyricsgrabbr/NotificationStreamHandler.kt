package com.illusionware.lyricsgrabbr.lyricsgrabbr

import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.EventChannel.EventSink
import android.content.*

class NotificationStreamHandler(private val context: Context) : StreamHandler {
  private var eventSink: EventSink? = null
  private var receiver: NotificationReceiver? = null
  private val listenerIntent = Intent(context, NotificationListener::class.java)

  // Called whenever the event channel is subscribed to in Flutter
  override fun onListen(o: Any?, eventSink: EventSink?) {
    this.eventSink = eventSink

    receiver = NotificationReceiver()
    val intentFilter = IntentFilter()
    intentFilter.addAction(NotificationListener.NOTIFICATION_INTENT)
    context.registerReceiver(receiver, intentFilter)

    context.startService(listenerIntent)
  }

  // Called whenever the event channel subscription is cancelled in Flutter
  override fun onCancel(o: Any?) {
    eventSink = null

    context.stopService(listenerIntent)
    context.unregisterReceiver(receiver)
  }

  internal inner class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val packageName = intent.getStringExtra(NotificationListener.NOTIFICATION_PACKAGE_NAME)
      val packageMessage = intent.getStringExtra(NotificationListener.NOTIFICATION_PACKAGE_MESSAGE)
      val packageText = intent.getStringExtra(NotificationListener.NOTIFICATION_PACKAGE_TEXT)
      val packageExtra = intent.getStringExtra(NotificationListener.NOTIFICATION_PACKAGE_EXTRA)
      val packageArt = intent.getStringExtra(NotificationListener.NOTIFICATION_PACKAGE_ART)
      val packagePlaybackState = intent.getIntExtra(NotificationListener.NOTIFICATION_PACKAGE_PLAYBACK_STATE, -1)
      val packageDuration = intent.getLongExtra(NotificationListener.NOTIFICATION_PACKAGE_DURATION, -1)
      val map = HashMap<String, Any>()
      map["packageName"] = packageName!!
      map["packageMessage"] = packageMessage!!
      map["packageText"] = packageText!!
      map["packageExtra"] = packageExtra!!
      map["packageArt"] = packageArt!!
      map["packagePlaybackState"] = packagePlaybackState
      map["packageDuration"] = packageDuration
      eventSink?.success(map)
    }
  }
}
