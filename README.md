# lyricsgrabbr
App that listens to your notifications and provides lyrics for the currently playing song.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.illusionman1212.lyricsgrabbr/)

## Supported players
Any player that uses the MediaSession API _should_ be supported

## Using Spotify for lyrics
If you plan on using the "Use Spotify for lyrics" feature you will need to create a Spotify developer app.
You can do so [here](https://developer.spotify.com/dashboard/applications), and you should follow these steps:
- Click on "Create app" in your developer dashboard
- Name your app whatever you'd like and give it a description
- Add the following redirect URI: `lyricsgrabbr://spotify-auth`
- Select `Android` under "Which API/SDKs are you planning to use?"
- Save your newly created app
- You will be redirected to your app page where you can click on "Settings" and find your Client ID under the "Basic Information" tab

You can then use the Client ID of your app inside LyricsGrabbr to authenticate with Spotify.

## License
The project is licensed under the GPL3 license while the assets (icon) are licensed under the creative commons license.
