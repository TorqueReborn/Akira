package com.ghostreborn.akira.anilist

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AnilistNetwork {
    fun trending(num: Int): String {
        val url = URL("https://graphql.anilist.co/")
        val query = "query {\n" +
                "  Page(perPage: $num) {\n" +
                "    media(type: ANIME, sort: TRENDING_DESC) {\n" +
                "      id\n" +
                "      title {\n" +
                "        english\n" +
                "      }\n" +
                "      coverImage {\n" +
                "        large\n" +
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
}