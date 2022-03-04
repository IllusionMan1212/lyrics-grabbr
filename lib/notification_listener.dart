import 'dart:async';
import 'package:flutter/services.dart';
import 'dart:io' show Platform;

class NotificationException implements Exception {
  final String _cause;

  NotificationException(this._cause);

  @override
  String toString() {
    return _cause;
  }
}

abstract class PlaybackState {
  static const STATE_INVALID = -1;
  static const STATE_PAUSED = 2;
  static const STATE_PLAYING = 3;
}

class NotificationEvent {
  final String artist;
  final String packageName;
  final String songTitle;
  final int playbackState;
  final int duration;

  final DateTime timeStamp;

  const NotificationEvent(
      {required this.packageName,
      required this.artist,
      required this.timeStamp,
      required this.songTitle,
      required this.playbackState,
      required this.duration});

  factory NotificationEvent.fromMap(Map<dynamic, dynamic> map) {
    DateTime time = DateTime.now();
    String name = map['packageName'];
    String message = map['packageMessage'];
    String text = map['packageText'];
    int playbackState = map['packagePlaybackState'];
    int duration = map['packageDuration'];

    return NotificationEvent(
        packageName: name,
        artist: message,
        timeStamp: time,
        songTitle: text,
        playbackState: playbackState,
        duration: duration);
  }
}

NotificationEvent _notificationEvent(dynamic data) {
  return NotificationEvent.fromMap(data);
}

class AndroidNotificationListener {
  static const EventChannel _notificationEventChannel =
      EventChannel('notifications.eventChannel');
  static const MethodChannel _methodChannel =
      MethodChannel('notifications/method');

  Stream<NotificationEvent> get notificationStream {
    if (Platform.isAndroid) {
      Stream<NotificationEvent> _notificationStream = _notificationEventChannel
          .receiveBroadcastStream()
          .map((event) => _notificationEvent(event));
      return _notificationStream;
    }
    throw NotificationException(
        'Notification API exclusively available on Android!');
  }

  static Future<bool?> get hasPermission async {
    return await _methodChannel.invokeMethod('plugin.hasPermission');
  }

  static Future<bool?> openPermissionSettings() async {
    return await _methodChannel.invokeMethod('plugin.openPermissionSettings');
  }
}
