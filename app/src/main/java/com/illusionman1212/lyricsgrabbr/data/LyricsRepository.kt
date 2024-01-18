package com.illusionman1212.lyricsgrabbr.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.illusionman1212.lyricsgrabbr.R
import com.illusionman1212.lyricsgrabbr.data.network.GET
import com.illusionman1212.lyricsgrabbr.data.network.NetworkHelper
import com.illusionman1212.lyricsgrabbr.utils.await
import it.skrape.core.htmlDocument
import it.skrape.selects.ElementNotFoundException
import it.skrape.selects.html5.div
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

        if (res.isSuccessful) {
            return Pair(json.decodeFromString(SuccessResponse.serializer(), res.body!!.string())
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
        // TODO: write a custom fetcher for skrape.it to use our OkHttp client
        //  and get rid of jsoup. Or better yet, maybe skrape.it will be fixed in the future
        //  and we can just use its built in BrowserFetcher
        val doc = Jsoup.connect(url.toString()).userAgent(USER_AGENT).get()

        try {
            val lyrics = htmlDocument(doc.toString()) {
                // set relaxed for instrumental songs
                relaxed = true

                findFirst {
                    div {
                        withAttributeKey = "data-lyrics-container"
                        findAll {
                            this
                        }.joinToString(separator = "\n") {
                            val accum = StringUtil.borrowBuilder()
                            it.element.text()

                            it.element.traverse(object : NodeVisitor {
                                override fun head(node: Node, depth: Int) {
                                    if (node is TextNode) {
                                        accum.append(node.text().trim())
                                        // append space here to get proper spacing when there's a <i> tag or other inline text tags
                                        accum.append(" ")
                                    } else if (node is Element) {
                                        if (accum.isNotEmpty() && node.normalName() == "br") {
                                            accum.append("\n")
                                        }
                                    }
                                }

                                override fun tail(node: Node, depth: Int) {
                                    // since we append a space after every text node, we only append a newline if
                                    // the last char isn't a space
                                    val lastCharIsWhitespace = accum.isNotEmpty() && accum[accum.length - 1] == ' '
                                    if (node is Element) {
                                        if (node.isBlock && node.nextSibling() is TextNode && !lastCharIsWhitespace) {
                                            accum.append("\n")
                                        }
                                    }
                                }
                            })

                            accum
                        }
                    }
                }.replace("\n\n\n", "\n\n")
            }

            return lyrics
        } catch (e: ElementNotFoundException) {
            // TODO: relaxed is broken in skrape.it, so we have to catch this exception
            return ""
        }
    }

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.3"
    }
}