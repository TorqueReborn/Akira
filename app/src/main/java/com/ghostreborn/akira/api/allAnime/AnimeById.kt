package com.ghostreborn.akira.api.allAnime

import com.ghostreborn.akira.model.Anime
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AnimeById {

    fun animeByID(showId: String): Anime {

        val variables = "\"id\":\"$showId\""
        val queryTypes = "\$id:String!"
        val query = "show(_id:\$id){_id,thumbnail}"
        val url = "https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allmanga.to")
        val rawJSON = connection.inputStream.bufferedReader().readText()

        val show = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("show")

        val id = show.getString("_id")
        val thumbnail = show.getString("thumbnail")
        return Anime(id, thumbnail)
    }

}