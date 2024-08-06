package com.ghostreborn.akira.gojo

import com.ghostreborn.akira.model.Anime
import org.json.JSONArray

class GojoParser {
    fun recentUpdates(): ArrayList<Anime>{
        val anime = ArrayList<Anime>()
        val recentUpdates = JSONArray(GojoNetwork().recentUpdates())
        for (i in 0 until recentUpdates.length()){
            recentUpdates.getJSONObject(i).apply {
                anime.add(Anime(getString("id"), getJSONObject("title").getString("english"), getString("coverImage")))
            }
        }
        return anime
    }
}