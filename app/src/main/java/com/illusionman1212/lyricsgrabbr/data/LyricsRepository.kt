package com.illusionman1212.lyricsgrabbr.data

import android.content.Context
import android.net.Uri
import com.illusionman1212.lyricsgrabbr.R
import com.illusionman1212.lyricsgrabbr.data.network.GET
import com.illusionman1212.lyricsgrabbr.data.network.NetworkHelper
import com.illusionman1212.lyricsgrabbr.utils.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

@Serializable
data class LyricLine(
    val startTimeMs: ULong,
    val words: String,
)

@Serializable
data class Lyrics(
    val syncType: String,
    val lines: List<LyricLine>,
    val language: String,
)

@Serializable
data class SuccessResponse(
    val lyrics: Lyrics,
)

class LyricsRepository(private val context: Context) {
    private val networkHelper = NetworkHelper(context)
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun fetchLyricsFromSpotify(trackId: String): Pair<String, Boolean> {
        val builder = Uri.Builder()
        val url = builder.scheme("https")
            .authority("api.lyricstify.vercel.app")
            .appendPath("v1")
            .appendPath("lyrics")
            .appendPath(trackId)
            .build()

        val res = networkHelper.client.newCall(
            GET(url.toString())
        ).await()

        val body = res.body?.string()

        if (res.isSuccessful) {
            return Pair(json.decodeFromString(SuccessResponse.serializer(), body!!)
                .lyrics
                .lines.joinToString(separator = "\n") { it.words }, true)
        } else {
            if (res.code == 404) {
                return Pair(context.resources.getString(R.string.spotify_no_lyrics_error), false)
            }

            return Pair(context.resources.getString(R.string.generic_error), false)
        }
    }

    fun fetchLyricsFromGenius(url: Uri): String {
        val doc = Jsoup.connect(url.toString()).userAgent(USER_AGENT).get()
        val divs = doc.select("div[data-lyrics-container]")

        val lyrics = StringUtil.borrowBuilder()

        divs.forEach {
            it.traverse(object : NodeVisitor {
                override fun head(node: Node, depth: Int) {
                    if (node is TextNode) {
                        lyrics.append(node.text().trim())
                        // append space here to get proper spacing when there's a <i> tag or other inline text tags
                        lyrics.append(" ")
                    } else if (node is Element) {
                        if (lyrics.isNotEmpty() && node.normalName() == "br") {
                            lyrics.append("\n")
                        }
                    }
                }

                override fun tail(node: Node, depth: Int) {
                    // since we append a space after every text node, we only append a newline if
                    // the last char isn't a space
                    val lastCharIsWhitespace = lyrics.isNotEmpty() && lyrics[lyrics.length - 1] == ' '
                    if (node is Element) {
                        if (node.isBlock && node.nextSibling() is TextNode && !lastCharIsWhitespace) {
                            lyrics.append("\n")
                        }
                    }
                }
            })

            lyrics.append("\n")
        }

        return lyrics.toString().replace("\n\n\n", "\n\n")
    }

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.3"
    }
}