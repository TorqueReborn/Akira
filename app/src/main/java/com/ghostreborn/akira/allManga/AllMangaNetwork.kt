package com.ghostreborn.akira.allManga

import okhttp3.OkHttpClient
import okhttp3.Request

class AllMangaNetwork {
    private fun connectAllAnime(
        variables: String,
        queryTypes: String,
        query: String
    ): String? {
        val client = OkHttpClient()
        var url = "https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}"
        val request = Request.Builder()
            .url(url)
            .header("Referer", "https://allanime.to")
            .header("Cipher", "AES256-SHA256")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0")
            .build()
        return client.newCall(request).execute().body?.string()
    }

    fun searchManga(anime: String): String? {
        val variables = "\"search\":{\"query\":\"$anime\"}"
        val queryTypes = "\$search:SearchInput"
        val query = "mangas(search:\$search){edges{_id,name,thumbnail}}"
        return connectAllAnime(variables, queryTypes, query)
    }
}