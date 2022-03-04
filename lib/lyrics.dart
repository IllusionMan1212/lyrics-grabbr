import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert' as convert;

class LyricsPage extends StatefulWidget {
  const LyricsPage({
    Key? key,
    required this.title,
    required this.artist,
    required this.url,
  }) : super(key: key);

  final String title;
  final String artist;
  final String url;

  @override
  State<StatefulWidget> createState() => _LyricsPageState();
}

class _LyricsPageState extends State<LyricsPage> {
  bool fetching = false;
  String? lyrics;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    setState(() => fetching = true);

    final uri = Uri.https('api.illusionman1212.tech', '/lyrics', {
      'songUrl': widget.url,
    });

    var response = await http.get(uri);
    if (response.statusCode == 200) {
      final jsonRes = convert.jsonDecode(response.body) as Map<String, dynamic>;

      setState(() {
        fetching = false;
        lyrics = jsonRes['lyrics'];
      });
    } else {
      print('error while GETing lyrics');
      setState(() => fetching = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              widget.title,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
              ),
            ),
            Text(
              widget.artist,
              style: const TextStyle(
                fontSize: 13,
              ),
            ),
          ],
        ),
      ),
      body: Center(
        child: fetching
            ? Column(
                children: [
                  Expanded(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: const [CircularProgressIndicator(value: null)],
                    ),
                  ),
                ],
              )
            : SizedBox(
                width: MediaQuery.of(context).size.width,
                child: SingleChildScrollView(
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 30, vertical: 30),
                    child: Text(
                      lyrics ?? '',
                      style: const TextStyle(
                        fontSize: 16,
                      ),
                    ),
                  ),
                ),
              ),
      ),
    );
  }
}
