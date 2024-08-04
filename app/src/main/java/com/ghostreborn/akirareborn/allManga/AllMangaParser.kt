package com.ghostreborn.akirareborn.allManga

import com.ghostreborn.akirareborn.model.Anime
import org.json.JSONObject

class AllMangaParser {
    fun searchManga(manga: String): ArrayList<Anime> {
        return ArrayList<Anime>().apply {
            val edgesArray = JSONObject(AllMangaNetwork().searchManga(manga).toString())
                .getJSONObject("data")
                .getJSONObject("mangas")
                .getJSONArray("edges")
            for (i in 0 until edgesArray.length()) {
                edgesArray.getJSONObject(i).let {
                    add(Anime(it.getString("_id"), it.getString("name"), it.getString("thumbnail")))
                }
            }
        }
    }
}