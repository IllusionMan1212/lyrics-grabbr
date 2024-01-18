package com.illusionman1212.lyricsgrabbr.data.network

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class NetworkHelper(context: Context) {
    private val cacheDir = File(context.cacheDir, "network_cache")
    private val cacheSize = 5L * 1024 * 1024 // 5 MiB

    private val baseClientBuilder: OkHttpClient.Builder
        get() {
            val builder = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .callTimeout(2, TimeUnit.MINUTES)

            return builder
        }

    val client by lazy { baseClientBuilder.cache(Cache(cacheDir, cacheSize)).build() }
}
