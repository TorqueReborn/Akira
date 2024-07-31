package com.ghostreborn.akirareborn.test

import okhttp3.OkHttpClient
import okhttp3.Request

class TestAPI {

    fun connectAllAnime(url: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .header("Referer", "https://allanime.to")
            .header("Cipher", "AES256-SHA256")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0"
            )
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        return responseBody
    }

}