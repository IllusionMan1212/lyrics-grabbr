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
  NotificationEvent? currentlyPlaying;
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

  void _showNotificationPermDialog() {
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
      _showNotificationPermDialog();
    }
  }

  void onData(NotificationEvent event) {
    setState(() {
      currentlyPlaying = event;
    });
  }

  void startListening() {
    _notifications = AndroidNotificationListener();

    _subscription = _notifications.notificationStream.listen(onData);
    setState(() => running = true);
  }

  void stopListening() {
    setState(() => running = false);
    _subscription.cancel();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Container(
        padding: const EdgeInsets.all(20),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.start,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              running ? RichText(
                text: TextSpan(
                    text: currentlyPlaying?.songTitle,
                    style: const TextStyle(
                        fontSize: 24.0,
                        color: Colors.black,
                        fontWeight: FontWeight.w700,
                    ),
                    children: [
                      const TextSpan(
                        text: "\n",
                      ),
                      TextSpan(
                          text: currentlyPlaying?.artist,
                          style: const TextStyle(
                            fontSize: 20.0,
                            color: Color(0xFF303030),
                          )
                      )
                    ]
                ),
                textAlign: TextAlign.center,
              ) :
              const Text("Listener stopped"),
              running ? Text(
                currentlyPlaying?.playbackState == PlaybackState.STATE_PLAYING ? "Spotify Playing" : "Spotify Paused",
                style: Theme.of(context).textTheme.headline6,
              ) :
              const Text("")
            ],
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: running ? stopListening : startListening,
        tooltip: running ? 'Stop listener' : "Start listener",
        child: running ? const Icon(Icons.stop) : const Icon(Icons.play_arrow),
      ),
    );
  }
}
