package com.ghostreborn.akirareborn.allAnime

import androidx.core.text.HtmlCompat
import com.ghostreborn.akirareborn.model.Anime
import com.ghostreborn.akirareborn.model.AnimeDetails
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

    fun animeDetails(animeId: String): AnimeDetails {
        val show: JSONObject = JSONObject(AllAnimeNetwork().animeDetails(animeId).toString())
            .getJSONObject("data")
            .getJSONObject("show")
        val name = show.getString("name")
        val thumbnail = show.getString("thumbnail")
        val description =
            HtmlCompat.fromHtml(show.getString("description"), HtmlCompat.FROM_HTML_MODE_COMPACT)
                .toString()
        val banner = show.getString("banner")
        var prequel = ""
        var sequel = ""
        val relatedShows = show.getJSONArray("relatedShows")
        for (i in 0 until relatedShows.length()) {
            val relatedShow = relatedShows.getJSONObject(i)
            val relation = relatedShow.getString("relation")
            if ("prequel" == relation) prequel = relatedShow.getString("showId")
            if ("sequel" == relation) sequel = relatedShow.getString("showId")
        }
        return AnimeDetails(name, thumbnail, description, banner, prequel, sequel)
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