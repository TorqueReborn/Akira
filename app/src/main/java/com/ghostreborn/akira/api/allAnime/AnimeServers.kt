package com.ghostreborn.akira.api.allAnime

import com.ghostreborn.akira.Utils
import com.ghostreborn.akira.model.Server
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AnimeServers {

    fun servers(id: String, episode: String): ArrayList<Server> {
        val variables = "\"showId\":\"$id\",\"episode\":\"$episode\",\"translationType\":\"sub\""
        val queryTypes = "\$showId:String!,\$episode:String!,\$translationType:VaildTranslationTypeEnumType!"
        val query =  "episode(showId:\$showId,episodeString:\$episode,translationType:\$translationType){sourceUrls}"
        var url = "https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allmanga.to")
        val rawJSON = connection.inputStream.bufferedReader().readText()

        val sources: ArrayList<Server> = ArrayList()
        val edges = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("episode")
            .getJSONArray("sourceUrls")
        for (i in 0 until edges.length()) {
            val edge = edges.getJSONObject(i)
            if(!edge.getString("sourceUrl").contains("--")) {
                continue
            }
            val decrypted = Utils().decrypt(edge.getString("sourceUrl"))
            if(decrypted.contains("fast4speed")) {
                continue
            }
            url = "https://allanime.day" + decrypted.replace("clock", "clock.json")
            sources.add(Server(edge.getString("sourceName"), url))
        }

        return sources
    }

}