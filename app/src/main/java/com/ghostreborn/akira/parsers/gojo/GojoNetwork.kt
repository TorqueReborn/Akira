package com.ghostreborn.akira.parsers.gojo

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GojoNetwork {
    fun search(anime:String):String {
        val url = URL("https://graphql.anilist.co/")
        val query = "query{\n" +
                "  Page(perPage: 39){\n" +
                "    media(search:\"${anime}\",type:ANIME){\n" +
                "      id\n" +
                "      title{\n" +
                "        userPreferred\n" +
                "      }\n" +
                "      coverImage {\n" +
                "        medium\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
        val jsonQuery = JSONObject().put("query", query)
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            OutputStreamWriter(outputStream).use { it.write(jsonQuery.toString()) }
            val response = StringBuilder()
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
            }
            return response.toString()
        }
    }

    fun recentlyUpdated():String {
        val url = URL("https://graphql.anilist.co/")
        val query = "query {\n" +
                "  Page(perPage: 39, page: 1) {\n" +
                "    media(sort: TRENDING_DESC, type: ANIME, status: RELEASING) {\n" +
                "      id\n" +
                "      title {\n" +
                "        userPreferred\n" +
                "      }\n" +
                "      coverImage {\n" +
                "        medium\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
        val jsonQuery = JSONObject().put("query", query)
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            OutputStreamWriter(outputStream).use { it.write(jsonQuery.toString()) }
            val response = StringBuilder()
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
            }
            return response.toString()
        }
    }

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