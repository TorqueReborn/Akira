package com.ghostreborn.akira.allAnime

import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.AnimeItem
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AnimeBySeasonYear {

    fun animeBySeasonYear(season: String, year: String): Anime {

        val animeList: ArrayList<AnimeItem> = ArrayList()

        val variables = "\"search\":{\"season\":\"$season\",\"year\":$year}"
        val queryTypes = "\$search:SearchInput!"
        val query = "shows(search:\$search){edges{_id,name,thumbnail}}"
        val url = "https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allmanga.to")
        val rawJSON = connection.inputStream.bufferedReader().readText()

        val edges = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("shows")
            .getJSONArray("edges")

        for(i in 0 until edges.length()) {
            val edge = edges.getJSONObject(i)
            val id = edge.getString("_id")
            val name = edge.getString("name")
            val thumbnail = edge.getString("thumbnail")
            animeList.add(AnimeItem(id, name, thumbnail))
        }

        return Anime("$season $year", animeList)

    }

}