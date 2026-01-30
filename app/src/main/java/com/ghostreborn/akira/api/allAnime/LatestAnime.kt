package com.ghostreborn.akira.api.allAnime

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

class LatestAnime {

    fun latestAnime() {
        val variables = "\"search\": {\"sortBy\": \"Recent\"}"
        val queryTypes = "\$search: SearchInput!"
        val query = "shows(search: \$search){edges{_id,name,thumbnail}}"
        val url = "https://api.allanime.day/api?variables={$variables}&query=query($queryTypes){$query}"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allmanga.to")
        val rawJSON = connection.inputStream.bufferedReader().readText()

        Log.e("TAG", rawJSON)
    }

}