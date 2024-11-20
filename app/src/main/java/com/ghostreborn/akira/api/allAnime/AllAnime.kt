package com.ghostreborn.akira.api.allAnime

import com.ghostreborn.akira.api.AnimeAPI
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

    fun allAnimeID(anime: String, anilistID: String): String {
        val variables = "\"search\":{\"allowAdult\":false,\"allowUnknown\":false,\"query\":\"$anime\"},\"limit\":20,\"page\":1,\"translationType\":\"sub\",\"countryOrigin\":\"ALL\""
        val queryTypes = "\$search:SearchInput,\$limit:Int,\$page:Int,\$translationType:VaildTranslationTypeEnumType,\$countryOrigin:VaildCountryOriginEnumType"
        val query = "shows(search:\$search,limit:\$limit,page:\$page,translationType:\$translationType,countryOrigin:\$countryOrigin){edges{_id,aniListId}}"
        val rawJSON = connectAllAnime(variables, queryTypes, query)
        val edges = JSONObject(rawJSON).getJSONObject("data").getJSONObject("shows").getJSONArray("edges")
        var allAnimeID = ""
        for (i in 0 until edges.length()) {
            if (edges.getJSONObject(i).getString("aniListId") == anilistID) {
               allAnimeID = edges.getJSONObject(i).getString("_id")
                break
            }
        }
        return allAnimeID
    }

    override fun episode(animeName: String, animeID: String): ArrayList<String> {
        var allAnimeID = allAnimeID(animeName, animeID)
        val variables = "\"showId\":\"$allAnimeID\""
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