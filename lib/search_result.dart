import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import 'lyrics.dart';

class SearchResult extends StatelessWidget {
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
  final Uri url;
  final Uri artistUrl;
  final String thumbnail;
  final int id;

  SearchResult.fromJson(Map<String, dynamic> json, {Key? key})
      : title = json['meta']['title'],
        artist = json['meta']['primaryArtist']['name'],
        url = Uri.parse(json['url']),
        artistUrl = Uri.parse(json['meta']['primaryArtist']['url']),
        thumbnail = json['resources']['thumbnail'],
        id = json['id'],
        super(key: key);

  List<Widget> _buildOptions() {
    return <OptionItem>[
      OptionItem(
          title: "Open Artist Page in Genius",
          icon: Icons.library_music,
          onTap: () async {
            if (!await launchUrl(artistUrl)) {
              throw 'could not launch $artistUrl';
            }
          }),
      OptionItem(
          title: "Open Song Page in Genius",
          icon: Icons.music_note,
          onTap: () async {
            if (!await launchUrl(url)) {
              throw 'could not launch $url';
            }
          })
    ];
  }

  void _showOptionsBottomSheet(BuildContext context) {
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
      child: InkWell(
        onTap: () {
          Navigator.of(context).push(
            MaterialPageRoute(
              builder: (context) => LyricsPage(
                title: title,
                artist: artist,
                url: url.toString(),
              ),
            ),
          );
        },
        onLongPress: () => _showOptionsBottomSheet(context),
        child: Ink(
          height: 75,
          child: Row(
            children: [
              Container(
                margin: const EdgeInsets.only(right: 20),
                child: Image.network(
                  thumbnail,
                  width: 75,
                  height: 75,
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stackTrace) {
                    return const Center(
                      child: SizedBox(
                          width: 75,
                          height: 75,
                          child: Icon(Icons.broken_image, size: 40)),
                    );
                  },
                ),
              ),
              Flexible(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      title,
                      style: TextStyle(
                        color: Theme.of(context).textTheme.bodyLarge?.color,
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                      overflow: TextOverflow.ellipsis,
                    ),
                    Text(
                      artist,
                      style: TextStyle(
                        color: Theme.of(context).textTheme.titleMedium?.color,
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
    );
  }
}

class OptionItem extends StatelessWidget {
  final String title;
  final IconData icon;
  final void Function()? onTap;

  const OptionItem(
      {Key? key, required this.title, required this.icon, this.onTap})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Ink(
        padding: const EdgeInsets.all(20),
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
