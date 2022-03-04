import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'notification_listener.dart';
import 'search_result.dart';
import 'package:http/http.dart' as http;
import 'dart:convert' as convert;

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
  BuildContext? permissionDialogCtx;
  NotificationEvent? lastNotification;
  bool running = false;
  bool searching = false;
  final List<SearchResult> searchResults = [];

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
                'This app requires permission to listen to notifications to function properly. Please grant permission in the next screen. BEWARE that the only way to stop the service is to revoke the notification listening permission.',
                style: TextStyle(color: Colors.blueGrey),
              ),
              actions: [
                TextButton(
                    onPressed: () async {
                      await AndroidNotificationListener
                          .openPermissionSettings();
                    },
                    child: const Text('OK')),
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
    if (event.playbackState == PlaybackState.STATE_INVALID) {
      return;
    }

    if (lastNotification?.songTitle != event.songTitle &&
        lastNotification?.artist != event.artist) {
      searchLyrics(event.songTitle, event.artist);

      setState(() {
        searchResults.clear();
        lastNotification = event;
      });
    }
  }

  void startListening() {
    _notifications = AndroidNotificationListener();

    _notifications.notificationStream.listen(onData);
    setState(() => running = true);
  }

  void searchLyrics(
    String song,
    String artist,
  ) async {
    setState(() {
      searching = true;
    });

    // TODO: build an httpclient to reuse between requests.
    final uri = Uri.https(
      'api.illusionman1212.tech',
      '/lyrics/search',
      {'q': '$song $artist'},
    );

    var response = await http.get(uri);
    if (response.statusCode == 200) {
      final jsonRes = convert.jsonDecode(response.body) as Map<String, dynamic>;
      List<dynamic> results = jsonRes['all'];

      for (var result in results) {
        var myResult = SearchResult.fromJson(result);
        setState(() {
          searchResults.add(myResult);
        });
      }

      setState(() {
        searching = false;
      });
    } else {
      // TODO: display error
      print('error in http get');
      setState(() {
        searching = false;
      });
    }
  }

  Widget _buildResults() {
    return ListView.builder(
      itemBuilder: (_, i) {
        return searchResults[i];
      },
      itemCount: searchResults.length,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        centerTitle: true,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            if (running)
              Container(
                padding: const EdgeInsets.all(20),
                child: RichText(
                  text: TextSpan(
                      text: lastNotification?.songTitle,
                      style: const TextStyle(
                        fontSize: 24.0,
                        color: Colors.black,
                        fontWeight: FontWeight.w700,
                      ),
                      children: [
                        const TextSpan(
                          text: '\n',
                        ),
                        TextSpan(
                          text: lastNotification?.artist,
                          style: const TextStyle(
                            fontSize: 20.0,
                            color: Color(0xFF303030),
                          ),
                        )
                      ]),
                  textAlign: TextAlign.center,
                ),
              ),
            Container(
              child: searching
                  ? Expanded(
                      child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: const [
                          CircularProgressIndicator(value: null)
                        ]))
                  : Flexible(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 10),
                            margin: const EdgeInsets.symmetric(vertical: 10),
                            child: Text(
                              running
                                  ? 'Found ${searchResults.length} result(s)'
                                  : "",
                              style: const TextStyle(
                                fontSize: 15,
                                color: Colors.black87,
                                fontWeight: FontWeight.bold,
                                fontStyle: FontStyle.italic,
                              ),
                            ),
                          ),
                          Flexible(child: _buildResults()),
                        ],
                      ),
                    ),
            ),
          ],
        ),
      ),
    );
  }
}
