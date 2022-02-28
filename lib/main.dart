import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'notification_listener.dart';

Future<void> main() async {
  runApp(const LyricsGrabbr());
}

class LyricsGrabbr extends StatelessWidget {
  const LyricsGrabbr({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Lyrics Grabbr',
      theme: ThemeData(
        primarySwatch: Colors.purple,
      ),
      home: const HomePage(title: 'Lyrics Grabbr'),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> with WidgetsBindingObserver {
  late AndroidNotificationListener _notifications;
  late StreamSubscription<NotificationEvent> _subscription;
  BuildContext? permissionDialogCtx;
  final List<NotificationEvent> _notifs = [];
  bool running = false;

  @override
  void initState() {
    WidgetsBinding.instance?.addObserver(this);
    super.initState();
    initPlatformState();
  }

  @override
  void dispose() {
    WidgetsBinding.instance?.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) async {
    if (state == AppLifecycleState.resumed) {
      if (permissionDialogCtx == null) {
        return;
      }

      var perm = await AndroidNotificationListener.hasPermission;

      if (perm != null && perm) {
        Navigator.pop(permissionDialogCtx!);
        startListening();
      }
    }
  }

  void showNotificationPermDialog() {
    showDialog(
        context: context,
        barrierDismissible: false,
        builder: (BuildContext buildCtx) {
          permissionDialogCtx = buildCtx;

          return WillPopScope(
            onWillPop: () {
              SystemNavigator.pop();
              return Future.value(false);
            },
              child: AlertDialog(
                title: const Text(
                  'Notifications Access Permission',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                content: const Text(
                  'This app requires permission to listen to notifications to function properly. Please grant permission in the next screen.',
                  style: TextStyle(color: Colors.blueGrey),
                ),
                actions: [
                  TextButton(
                      onPressed: () async {
                        await AndroidNotificationListener
                            .openPermissionSettings();
                      },
                      child: const Text("OK")),
                ],
              ),
          );
        });
  }

  Future<void> initPlatformState() async {
    var perm = await AndroidNotificationListener.hasPermission;

    if (perm != null && perm) {
      startListening();
    } else {
      showNotificationPermDialog();
    }
  }

  void onData(NotificationEvent event) {
    // TODO: if the app is in the background, only update our notif
    // if the app is in the foreground, update the notif, and
    // fetch the new lyrics and display them.
    setState(() {
      _notifs.add(event);
    });
  }

  void startListening() {
    // TODO: make the FAB start and stop a local notification service
    // that keeps track of which song is playing
    // and if pressed will fetch the lyrics and display them.

    //_notifications = AndroidNotificationListener();

    //_subscription = _notifications.notificationStream.listen(onData);
    AndroidNotificationListener.startService();
    setState(() => running = true);
  }

  void stopListening() {
    setState(() => running = false);
    AndroidNotificationListener.stopService();
    //_subscription.cancel();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: ListView.builder(
          itemCount: _notifs.length,
          reverse: true,
          itemBuilder: (BuildContext context, int idx) {
            final entry = _notifs[idx];
            return ListTile(
              leading: Text(entry.songTitle),
              trailing: Text(entry.artist),
            );
          }),
      floatingActionButton: FloatingActionButton(
        onPressed: running ? stopListening : startListening,
        tooltip: running ? 'Stop service' : "Start service",
        child: running ? const Icon(Icons.stop) : const Icon(Icons.play_arrow),
      ),
    );
  }
}
