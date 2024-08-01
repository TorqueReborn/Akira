package com.ghostreborn.akirareborn.allAnime

import org.json.JSONObject

class AllAnimeParser {
    fun allAnimeIdWithMalId(anime: String, malId: String):String {
        val rawJSON = AllAnimeNetwork().allAnimeIdWithMalId(anime).toString()
        val edgesArray = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("shows")
            .getJSONArray("edges")
        for (i in 0 until edgesArray.length()) {
            val edge = edgesArray.getJSONObject(i)
            if (edge.getString("malId") == malId){
                return edge.getString("_id")
            }
        }
        return ""
    }
}