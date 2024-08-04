package com.ghostreborn.akira.allAnime

import okhttp3.OkHttpClient
import okhttp3.Request

class TestAllAnime {

    fun connectAllAnime(
        variables: String,
        queryTypes: String,
        query: String
    ): String? {
        val client = OkHttpClient()
        val url = "https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}"
        val test = "https://api.allanime.day/api?variables={%22mangaId%22:%22ex9vXC6gWYY9bGkSo%22,%22translationType%22:%22sub%22,%22chapterString%22:%221073%22,%22limit%22:10,%22offset%22:0}&extensions={%22persistedQuery%22:{%22version%22:1,%22sha256Hash%22:%22121996b57011b69386b65ca8fc9e202046fc20bf68b8c8128de0d0e92a681195%22}}"
        val request = Request.Builder()
            .url(url)
            .header("Referer", "https://allanime.to")
            .header("Cipher", "AES256-SHA256")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0")
            .build()
        return client.newCall(request).execute().body?.string()
    }

    fun mangaChapters(mangaId: String): String?{
        val variables = "\"id\":\"$mangaId\""
        val queryTypes = "\$id:String!"
        val query = "manga(_id:\$id){availableChaptersDetail}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun chapterPages(mangaId: String, chapter: String): String?{
        val variables = "\"mangaId\":\"$mangaId\",\"chapterString\":\"$chapter\",\"translationType\":\"sub\""
        val queryTypes = "\$mangaId:String!,\$chapterString:String!,\$translationType:VaildTranslationTypeMangaEnumType!"
        val query = "chapterPages(mangaId:\$mangaId,chapterString:\$chapterString,translationType:\$translationType){edges{pictureUrls}}"
        return connectAllAnime(variables, queryTypes, query)
    }

}