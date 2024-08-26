package com.illusionman1212.lyricsgrabbr.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.illusionman1212.lyricsgrabbr.R
import com.illusionman1212.lyricsgrabbr.data.network.GET
import com.illusionman1212.lyricsgrabbr.data.network.NetworkHelper
import com.illusionman1212.lyricsgrabbr.utils.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.IOException
import org.jsoup.SerializationException
import java.net.SocketTimeoutException

@Serializable
data class ErrorResponse(
    val error: Boolean,
    val matches: Int?,
)

class HomeRepository(private val context: Context) {
    private val networkHelper = NetworkHelper(context)
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun isPermissionGranted(): Boolean {
        val packageName = context.packageName
        val pkgs = NotificationManagerCompat.getEnabledListenerPackages(context)

        return pkgs.contains(packageName)
    }

    suspend fun makeSearchRequest(lContext: Context, baseUrl: String, song: String, artist: String): Pair<String?, Boolean> {
        val url = Uri.parse(baseUrl)
            .buildUpon()
            .appendPath("search")
            .appendQueryParameter("q", "$song $artist")
            .appendQueryParameter("disableFuzzy", "true")
            .build()

        try {
            val res = networkHelper.client.newCall(
                GET(url.toString())
            ).await()

            if (!res.isSuccessful) {
                if (res.code >= 500) {
                    return Pair(lContext.resources.getString(R.string.internal_server_error), false)
                }

                if (res.body?.contentType()?.type != "text" && res.body?.contentType()?.subtype != "json") {
                    return Pair(lContext.resources.getString(R.string.invalid_json), false)
                }

                try {
                    val errRes = json.decodeFromString(ErrorResponse.serializer(), res.body!!.string())

                    // matches is only null if there's an error. otherwise it's 0 when there are no results
                    if (errRes.matches == null) {
                        return Pair(lContext.resources.getString(R.string.generic_error), false)
                    }
                } catch (e: IllegalArgumentException) {
                    return Pair(lContext.resources.getString(R.string.failed_to_map_json), false)
                }

                return Pair(null, false)
            }

            return Pair(res.body?.string(), true)
        } catch (e: IOException) {
            if (e.cause is SocketTimeoutException) {
                return Pair(lContext.resources.getString(R.string.timeout), false)
            }

            return Pair(e.message, false)
        }
    }
}
