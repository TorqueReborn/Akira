package com.ghostreborn.akira.allAnime

import com.ghostreborn.akira.fragment.SeasonalFragment
import com.ghostreborn.akira.model.Anime
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AnimeSearch {

    fun animeSearch(search: String): ArrayList<Anime> {
        val animeList: ArrayList<Anime> = ArrayList()

        val variables = "\"search\":{\"query\":\"$search\"},\"limit\":12,\"page\":${SeasonalFragment.page}"
        val queryTypes = "\$search:SearchInput!, \$limit:Int!, \$page:Int!"
        val query = "shows(search:\$search, limit:\$limit, page:\$page){edges{_id,thumbnail}}"
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
            var thumbnail = edge.getString("thumbnail")
            if (!thumbnail.contains("https")) {
                thumbnail = "https://wp.youtube-anime.com/aln.youtube-anime.com/$thumbnail"
            }
            animeList.add(Anime(id, thumbnail))
        }
        SeasonalFragment.page++
        return animeList
    }

}