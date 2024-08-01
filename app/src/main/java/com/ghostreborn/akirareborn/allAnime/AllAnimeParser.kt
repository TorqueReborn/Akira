package com.ghostreborn.akirareborn.allAnime

import com.ghostreborn.akirareborn.model.Anime
import org.json.JSONObject

class AllAnimeParser {
    fun searchAnime(anime: String): ArrayList<Anime> {
        val animeList: ArrayList<Anime> = ArrayList()
        val edgesArray = JSONObject(AllAnimeNetwork().searchAnime(anime).toString())
            .getJSONObject("data")
            .getJSONObject("shows")
            .getJSONArray("edges")
        for (i in 0 until edgesArray.length()) {
            val edge = edgesArray.getJSONObject(i)
            val id = edge.getString("_id")
            val name = edge.getString("name")
            val thumbnail = edge.getString("thumbnail")
            animeList.add(Anime(id, name, thumbnail))
        }
        return animeList
    }
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