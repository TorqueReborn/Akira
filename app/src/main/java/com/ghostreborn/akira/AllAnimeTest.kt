package com.ghostreborn.akira

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AllAnimeTest {

    fun connectAllAnime(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allmanga.to")
        return connection.inputStream.bufferedReader().readText()
    }

    fun getSomething(): String {
        val rawJSON = connectAllAnime("https://api.allanime.day/api?variables={%22search%22:{%22season%22:%22Spring%22,%22year%22:2025},%22limit%22:26,%22page%22:1,%22translationType%22:%22sub%22,%22countryOrigin%22:%22JP%22}&extensions={%22persistedQuery%22:{%22version%22:1,%22sha256Hash%22:%2206327bc10dd682e1ee7e07b6db9c16e9ad2fd56c1b769e47513128cd5c9fc77a%22}}")
        val edges = JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("shows")
            .getJSONArray("edges")
        val out = StringBuilder()
        for (i in 0 until edges.length()) {
            out.append(edges.getJSONObject(i)).append("\n\n")
        }
        return out.toString()
    }

}