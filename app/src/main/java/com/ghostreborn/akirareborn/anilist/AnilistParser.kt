package com.ghostreborn.akirareborn.anilist

import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.model.Anilist
import org.json.JSONObject

class AnilistParser {
    fun getAnimeList(type: String, status: String): String? {
        Constants.anilistAnimeList = ArrayList()
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
            val mediaId = entry.getString("id")
            Constants.anilistAnimeList.add(Anilist(malID, mediaId, progress))
        }
        return rawJSON
    }
}