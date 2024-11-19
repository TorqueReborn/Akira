package com.ghostreborn.akira.api

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AllAnime: AnimeAPI() {

    private fun connectAllAnime(
        variables: String,
        queryTypes: String,
        query: String
    ): String {
        val url = URL("https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allanime.to")
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    override fun episode(animeID: String): ArrayList<String> {
        val variables = "\"showId\":\"$animeID\""
        val queryTypes = "\$showId:String!"
        val query = "show(_id:\$showId){availableEpisodesDetail}"
        val rawJSON = connectAllAnime(variables, queryTypes, query)
        val episodesArray = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("show")
            .getJSONObject("availableEpisodesDetail")
            .getJSONArray("sub")
        val episodeList = ArrayList<String>().apply {
            for (i in episodesArray.length() - 1 downTo 0) {
                add(episodesArray.getString(i))
            }
        }
        return episodeList
    }

    override fun server(
        animeID: String,
        episodeNum: String,
        episodeID: String
    ): ArrayList<String> {
        val variables = "\"showId\":\"$animeID\",\"episode\":\"$episodeNum\",\"translationType\":\"sub\""
        val queryTypes = "\$showId:String!,\$episode:String!,\$translationType:VaildTranslationTypeEnumType!"
        val query = "episode(showId:\$showId,episodeString:\$episode,translationType:\$translationType){sourceUrls}"
        val rawJSON = connectAllAnime(variables, queryTypes, query)
        val sourceUrls = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("episode")
            .getJSONArray("sourceUrls")

        return (0 until sourceUrls.length())
            .map { sourceUrls.getJSONObject(it).getString("sourceUrl") }
            .filter { it.contains("--") }
            .map {
                val decrypted =
                    decryptAllAnimeServer(it.substring(2)).replace("clock", "clock.json")
                if (!decrypted.contains("fast4speed")) "https://allanime.day$decrypted" else ""
            }
            .filter { it.isNotEmpty() } as ArrayList<String>
    }

    private fun decryptAllAnimeServer(decrypt: String): String {
        return buildString {
            for (i in decrypt.indices step 2) {
                val dec = decrypt.substring(i, i + 2).toInt(16)
                append((dec xor 56).toChar())
            }
        }
    }
}