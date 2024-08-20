package com.ghostreborn.akira.utils

import okhttp3.OkHttpClient
import okhttp3.Request

class AkiraUtils {
    fun getLatestPackage(): String? {
        val client = OkHttpClient()
        val url = "https://api.github.com/repos/TorqueReborn/Akira/releases/latest"
        val request = Request.Builder()
            .url(url)
            .build()
        return client.newCall(request).execute().body?.string()
    }
}