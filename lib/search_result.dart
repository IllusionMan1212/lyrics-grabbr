import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import 'lyrics.dart';

class SearchResult extends StatefulWidget {
  const SearchResult(
      {Key? key,
      required this.title,
      required this.artist,
      required this.url,
      required this.artistUrl,
      required this.thumbnail,
      required this.id})
      : super(key: key);

  final String title;
  final String artist;
  final String url;
  final String artistUrl;
  final String thumbnail;
  final int id;

  SearchResult.fromJson(Map<String, dynamic> json, {Key? key})
      : title = json['meta']['title'],
        artist = json['meta']['primaryArtist']['name'],
        url = json['url'],
        artistUrl = json['meta']['primaryArtist']['url'],
        thumbnail = json['resources']['thumbnail'],
        id = json['id'],
        super(key: key);

  @override
  State<StatefulWidget> createState() => _SearchResultState();
}

class OptionItem extends StatelessWidget {
  final String title;
  final IconData icon;
  final void Function()? tapFunction;

  const OptionItem(
      {Key? key, required this.title, required this.icon, this.tapFunction})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: tapFunction,
      child: Ink(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
        child: Row(
          children: [
            Container(
              margin: const EdgeInsets.only(right: 15),
              child: Icon(icon),
            ),
            Text(
              title,
              style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w600),
            ),
          ],
        ),
      ),
    );
  }
}

class _SearchResultState extends State<SearchResult> {
  List<Widget> _buildOptions() {
    return <OptionItem>[
      OptionItem(
          title: "Open Artist Page in Genius",
          icon: Icons.library_music,
          tapFunction: () async {
            if (!await launch(widget.artistUrl)) {
              throw 'could not launch ${widget.artistUrl}';
            }
          }),
      OptionItem(
          title: "Open Song Page in Genius",
          icon: Icons.music_note,
          tapFunction: () async {
            if (!await launch(widget.url)) {
              throw 'could not launch ${widget.url}';
            }
          })
    ];
  }

  void _showOptionsBottomSheet() {
    showModalBottomSheet(
        context: context,
        builder: (context) {
          return ConstrainedBox(
            constraints: BoxConstraints(
              maxHeight:
                  (MediaQuery.of(context).size.height - kToolbarHeight) / 2,
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: _buildOptions(),
            ),
          );
        });
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      child: Container(
        margin: const EdgeInsets.only(top: 0.5),
        child: InkWell(
          onTap: () {
            Navigator.of(context).push(
              MaterialPageRoute(
                builder: (context) => LyricsPage(
                  title: widget.title,
                  artist: widget.artist,
                  url: widget.url,
                ),
              ),
            );
          },
          onLongPress: () => _showOptionsBottomSheet(),
          child: Ink(
            height: 75,
            color: Colors.black,
            child: Row(
              children: [
                Container(
                  margin: const EdgeInsets.only(right: 20),
                  child: Image.network(
                    widget.thumbnail,
                    width: 75,
                    height: 75,
                    fit: BoxFit.cover,
                  ),
                ),
                Flexible(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        widget.title,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                        overflow: TextOverflow.ellipsis,
                      ),
                      Text(
                        widget.artist,
                        style: const TextStyle(
                          color: Colors.white70,
                          fontSize: 14,
                        ),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
