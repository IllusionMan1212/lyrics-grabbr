import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'notification_listener.dart';
import 'search_result.dart';
import 'package:http/http.dart' as http;
import 'dart:convert' as convert;
import 'package:shared_preferences/shared_preferences.dart';

import 'settings.dart';
import 'theme_provider.dart';

Future<void> main() async {
  runApp(const LyricsGrabbr());
}

class LyricsGrabbr extends StatefulWidget {
  const LyricsGrabbr({Key? key}) : super(key: key);

  @override
  State<LyricsGrabbr> createState() => _LyricsGrabbrState();
}

class _LyricsGrabbrState extends State<LyricsGrabbr> {
  @override
  Widget build(BuildContext context) {
    return FutureBuilder<SharedPreferences>(
      future: SharedPreferences.getInstance(),
      builder: (_, snapshot) {
      final prefTheme = snapshot.data?.getString("theme");

      var themeMode = ThemeMode.system;
      if (prefTheme == AppTheme.Dark.name) {
          themeMode = ThemeMode.dark;
      } else if (prefTheme == AppTheme.Light.name) {
          themeMode = ThemeMode.light;
      } else if (prefTheme == AppTheme.System.name) {
          themeMode = ThemeMode.system;
      }

      return snapshot.hasData ? ChangeNotifierProvider(
        create: (context) => ThemeProvider(themeMode),
        builder: (context, _) {
          final themeProvider = Provider.of<ThemeProvider>(context);

          return MaterialApp(
            title: 'Lyrics Grabbr',
            theme: MyThemes.lightTheme,
            darkTheme: MyThemes.darkTheme,
            themeMode: themeProvider.themeMode,
            home: const HomePage(title: 'Lyrics Grabbr'),
          );
        }
      ) : Container();
      }
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
    WidgetsBinding.instance.addObserver(this);
    super.initState();
    initPlatformState();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
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
    return ListView.separated(
      itemBuilder: (_, i) {
        return searchResults[i];
      },
      itemCount: searchResults.length,
      separatorBuilder: (BuildContext context, int index) {
        return const Divider(height: 1);
      },
    );
  }

  List<Widget> _buildPage() {
    if (lastNotification != null) {
      return [
        Container(
          padding: const EdgeInsets.all(20),
          child: RichText(
            text: TextSpan(
                text: lastNotification?.songTitle,
                style: TextStyle(
                  fontSize: 24.0,
                  color: Theme.of(context).textTheme.bodyLarge?.color,
                  fontWeight: FontWeight.w700,
                ),
                children: [
                  const TextSpan(
                    text: '\n',
                  ),
                  TextSpan(
                    text: lastNotification?.artist,
                    style: TextStyle(
                      fontSize: 20.0,
                      color: Theme.of(context).textTheme.bodyMedium?.color,
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
                      children: const [CircularProgressIndicator(value: null)]))
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
                          style: TextStyle(
                            fontSize: 15,
                            color: Theme.of(context).textTheme.bodyText1?.color,
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
      ];
    }

    return [
      const Padding(
        padding: EdgeInsets.symmetric(vertical: 16),
        child: Text("No supported player is running",
            style: TextStyle(fontSize: 24)),
      ),
    ];
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            tooltip: "Settings",
            splashRadius: 20,
            onPressed: () {
              Navigator.of(context).push(
                  MaterialPageRoute(builder: (context) => const SettingsPage()));
            },
          ),
        ],
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: _buildPage(),
        ),
      ),
    );
  }
}
