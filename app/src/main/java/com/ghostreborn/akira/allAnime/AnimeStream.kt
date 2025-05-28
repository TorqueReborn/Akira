package com.ghostreborn.akira.allAnime

import com.ghostreborn.akira.model.SourceName
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AnimeStream {

    fun connectAllAnime(id: String, episode: String): ArrayList<SourceName> {
        val variables = "\"showId\":\"$id\",\"episode\":\"$episode\",\"translationType\":\"sub\""
        val queryTypes = "\$showId:String!,\$episode:String!,\$translationType:VaildTranslationTypeEnumType!"
        val query =  "episode(showId:\$showId,episodeString:\$episode,translationType:\$translationType){sourceUrls}"
        val url = "https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allmanga.to")
        val rawJSON = connection.inputStream.bufferedReader().readText()

        val sources: ArrayList<SourceName> = ArrayList()
        val edges = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("episode")
            .getJSONArray("sourceUrls")
        for (i in 0 until edges.length()) {
            val edge = edges.getJSONObject(i)
            sources.add(SourceName(edge.getString("sourceUrl"), edge.getString("sourceName")))
        }
        return sources
    }

}