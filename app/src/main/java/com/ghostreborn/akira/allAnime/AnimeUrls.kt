package com.ghostreborn.akira.allAnime

import com.ghostreborn.akira.model.Server
import okio.IOException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AnimeUrls {

    fun urls(url: String): ArrayList<Server> {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allmanga.to")
        val rawJSON: String
        try{
            rawJSON = connection.inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            e.printStackTrace()
            return ArrayList()
        }

        val urls: ArrayList<Server> = ArrayList()
        val links = JSONObject(rawJSON)
            .getJSONArray("links")
        for (i in 0 until links.length()) {
            val link = links.getJSONObject(i)
            urls.add(Server(link.getString("resolutionStr"), link.getString("link")))
        }
        return urls
    }

}