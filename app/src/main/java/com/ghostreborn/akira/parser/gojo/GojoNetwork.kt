package com.ghostreborn.akira.parser.gojo

import java.net.HttpURLConnection
import java.net.URL

class GojoNetwork {
    fun episodes(id:String): String{
        val url = URL("https://api.gojo.live/episodes?id=${id}")
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        return connection.inputStream.bufferedReader().use {
            it.readText()
        }
    }

    fun servers(id:String, episode:String, watchId:String):String{
        val url = URL("https://api.gojo.live/tiddies?id=${id}&provider=shash&subType=sub&num=${episode}&watchId=${watchId}")
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        return connection.inputStream.bufferedReader().use {
            it.readText()
        }
    }
}