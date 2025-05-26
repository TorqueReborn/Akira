package com.ghostreborn.akira

import java.net.HttpURLConnection
import java.net.URL

class AllAnimeTest {

    fun connectAllAnime(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allmanga.to")
        return connection.inputStream.bufferedReader().readText()
    }

}