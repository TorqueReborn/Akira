package com.ghostreborn.akira.anilist

import com.ghostreborn.akira.model.Anime
import org.json.JSONObject

class AnilistParser {
    fun trending(num: Int): ArrayList<Anime> {
        val search = AnilistNetwork().trending(num)
        val mediaArray = JSONObject(search)
            .getJSONObject("data")
            .getJSONObject("Page")
            .getJSONArray("media")
        val animes = ArrayList<Anime>()
        for (i in 0 until mediaArray.length()) {
            val media = mediaArray.getJSONObject(i)
            val title = media.getJSONObject("title")
                .getString("english")
            val image = media.getJSONObject("coverImage")
                .getString("large")
            animes.add(Anime(title, "0/1000", image))
        }
        return animes
    }
}