package com.ghostreborn.akirareborn.test

import android.util.Log
import com.ghostreborn.akirareborn.allanime.AllAnimeNetwork
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class TestAPI {

    private fun connectAllAnime(
        variables: String,
        queryTypes: String,
        query: String
    ): String? {
        val client = OkHttpClient()
        val url =
            "https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}"
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

    fun episodeUrls(id: String, episode: String): String? {
        val variables = "\"showId\":\"$id\",\"episode\":\"$episode\",\"translationType\":\"sub\""
        val queryTypes =
            "\$showId:String!,\$episode:String!,\$translationType:VaildTranslationTypeEnumType!"
        val query =
            "episode(showId:\$showId,episodeString:\$episode,translationType:\$translationType){" +
                    "sourceUrls" +
                    "}"
        return connectAllAnime(variables, queryTypes, query)
    }

    fun getJSON(url: String?): String {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url!!)
            .header("Referer", "https://allanime.to")
            .header("Cipher", "AES256-SHA256")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0"
            )
            .build()

        try {
            client.newCall(request).execute().use { response ->
                return if (response.body != null) response.body!!
                    .string() else "NULL"
            }
        } catch (e: IOException) {
            Log.e("TAG", "Error fetching JSON: ", e)
        }
        return "{}"
    }

    fun parse(id: String, episode: String): ArrayList<String> {
        val rawJSON = AllAnimeNetwork().episodeUrls(id, episode).toString()
        val sources = ArrayList<String>()
        val sourceUrls = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("episode")
            .getJSONArray("sourceUrls")
        for (i in 0 until sourceUrls.length()) {
            val sourceUrl = sourceUrls.getJSONObject(i).getString("sourceUrl")
            if (sourceUrl.contains("--")) {
                val decrypted: String =
                    decryptAllAnimeServer(sourceUrl.substring(2)).replace("clock", "clock.json")
                if (!decrypted.contains("fast4speed")) {
                    sources.add("https://allanime.day$decrypted")
                }
            }
        }
        return sources
    }

    private fun decryptAllAnimeServer(decrypt: String): String {
        val decryptedString = StringBuilder()
        var i = 0
        while (i < decrypt.length) {
            val dec = decrypt.substring(i, i + 2).toInt(16)
            decryptedString.append((dec xor 56).toChar())
            i += 2
        }
        return decryptedString.toString()
    }

}