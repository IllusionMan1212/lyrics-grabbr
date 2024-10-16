package com.illusionman1212.lyricsgrabbr.data

import android.net.Uri
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

class LyricsRepository {
    fun fetchLyricsFromGenius(url: Uri): String {
        return try {
            val doc = Jsoup.connect(url.toString())
                .userAgent(USER_AGENT)
                .get()

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

            lyrics.toString().replace("\n\n\n", "\n\n")

        } catch (e: HttpStatusException) {
            when (e.statusCode) {
                403 -> {
                    println("Access Forbidden: HTTP 403")
                    "Error 403: Cloudflare got in the way, try disabling the VPN or Proxy if enabled"
                }
                else -> {
                    println("HTTP Error: ${e.statusCode}")
                    "HTTP Error: ${e.statusCode}"
                }
            }
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
            "An error occurred while fetching lyrics."
        }
    }

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.3"
    }
}