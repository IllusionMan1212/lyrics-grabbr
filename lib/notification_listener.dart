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

class NotificationEvent {
  String packageMessage;
  String packageName;
  String packageExtra;
  String packageText;
  DateTime timeStamp;

  NotificationEvent({
    required this.packageName,
    required this.packageMessage,
    required this.timeStamp,
    required this.packageExtra,
    required this.packageText
  });

  factory NotificationEvent.fromMap(Map<dynamic, dynamic> map) {
    DateTime time = DateTime.now();
    String name = map['packageName'];
    String message = map['packageMessage'];
    String text = map['packageText'];
    String extra =  map['packageExtra'];

    return NotificationEvent(packageName: name, packageMessage: message, timeStamp: time,packageText: text , packageExtra: extra);
  }

  @override
  String toString() {
    return "Notification Event \n Package Name: $packageName \n - Timestamp: $timeStamp \n - Package Message: $packageMessage";
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
