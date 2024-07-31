package com.ghostreborn.akirareborn.anilist

import org.json.JSONObject

class AnilistParser {
    fun getAnimeList(type: String, status: String): String? {
        val rawJSON = AnilistNetwork().getAnimeList(type, status)
        val entries = JSONObject(rawJSON.toString())
            .getJSONObject("data")
            .getJSONObject("MediaListCollection")
            .getJSONArray("lists")
            .getJSONObject(0)
            .getJSONArray("entries")
        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val malID = entry.getJSONObject("media").getString("idMal")
            val progress = entry.getString("progress")
        }
        return rawJSON
    }
}