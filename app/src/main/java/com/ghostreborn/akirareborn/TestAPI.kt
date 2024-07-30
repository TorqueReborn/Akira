package com.ghostreborn.akirareborn

import okhttp3.OkHttpClient
import okhttp3.Request

class TestAPI {

    fun connectAllAnime(): String? {
        val client = OkHttpClient()
        val url = "https://api.allanime.day/api?variables={%22search%22:{%22sortBy%22:%22Recent%22},%22limit%22:26,%22page%22:1,%22translationType%22:%22sub%22,%22countryOrigin%22:%22ALL%22}&extensions={%22persistedQuery%22:{%22version%22:1,%22sha256Hash%22:%2206327bc10dd682e1ee7e07b6db9c16e9ad2fd56c1b769e47513128cd5c9fc77a%22}}"
        val request = Request.Builder()
            .url(url)
            .header("Referer", "https://allanime.to")
            .header("Cipher", "AES256-SHA256")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0")
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        return responseBody
    }

}