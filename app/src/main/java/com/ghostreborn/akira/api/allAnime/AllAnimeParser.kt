package com.ghostreborn.akira.api.allAnime

import org.json.JSONObject

class AllAnimeParser {

    fun allAnimeID(anime: String, anilistID: String): String {
        val edges = JSONObject(AllAnimeNetwork().allAnimeID(anime)).getJSONObject("data").getJSONObject("shows").getJSONArray("edges")
        var allAnimeID = ""
        for (i in 0 until edges.length()) {
            if (edges.getJSONObject(i).getString("aniListId") == anilistID) {
                allAnimeID = edges.getJSONObject(i).getString("_id")
                break
            }
        }
        return allAnimeID
    }

    fun episodes(anime: String, aniListID: String): ArrayList<ArrayList<String>> {
        val id = allAnimeID(anime, aniListID)
        val episodesArray = JSONObject(AllAnimeNetwork().episodes(id).toString())
            .getJSONObject("data")
            .getJSONObject("show")
            .getJSONObject("availableEpisodesDetail")
            .getJSONArray("sub")

        val episodeList = ArrayList<String>().apply {
            for (i in episodesArray.length() - 1 downTo 0) {
                add(episodesArray.getString(i))
            }
        }
        return groupEpisodes(episodeList)
    }

    private fun groupEpisodes(episodeList: ArrayList<String>): ArrayList<ArrayList<String>> {
        val group = ArrayList<ArrayList<String>>()
        var startIndex = 0
        while (startIndex < episodeList.size) {
            val endIndex = (startIndex + 15).coerceAtMost(episodeList.size)
            group.add(ArrayList(episodeList.subList(startIndex, endIndex)))
            startIndex = endIndex
        }
        return group
    }

    fun server(animeID: String, episode: String): ArrayList<String> {
        val sourceUrls = JSONObject(AllAnimeNetwork().episodeUrls(animeID, episode))
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