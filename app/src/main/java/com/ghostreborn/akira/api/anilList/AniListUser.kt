package com.ghostreborn.akira.api.anilList

import android.util.Log
import com.ghostreborn.akira.MainActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class AniListUser {

    private fun connectAniList(graph: String, token: String): String? {
        if(!MainActivity.internetAvailable) return null

        val url = URL("https://graphql.anilist.co")
        var connection: HttpURLConnection? = null

        try {
            connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Authorization", "Bearer $token")

            val jsonObject = JSONObject()
                .put("query", graph)

            connection.outputStream.use { os ->
                val input = jsonObject.toString().toByteArray(StandardCharsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return connection.inputStream.bufferedReader().readText()
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error connecting to AniList: ${e.message}")
        } finally {
            connection?.disconnect()
        }
        return "{}"
    }

    fun userList(userID: String, token: String): String? {
        val graph = "query{\n" +
                "  MediaListCollection(userId:" + userID + ",type:ANIME,status:CURRENT){\n" +
                "    lists{\n" +
                "      entries {\n" +
                "        media{ \n" +
                "          idMal\n" +
                "          title {\n" +
                "            userPreferred\n" +
                "          }\n" +
                "        }\n" +
                "        progress\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
        return connectAniList(graph, token)
    }

}