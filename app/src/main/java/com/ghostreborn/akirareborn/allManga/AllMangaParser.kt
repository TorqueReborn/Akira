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
                    var thumbnail = it.getString("thumbnail")
                    if (!thumbnail.startsWith("http")) {
                        thumbnail =
                            "https://wp.youtube-anime.com/aln.youtube-anime.com/${it.getString("thumbnail")}"
                    }
                    add(Anime(it.getString("_id"), it.getString("name"), thumbnail.toString()))
                }
            }
        }
    }
}