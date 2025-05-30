package com.ghostreborn.akira.anilList

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class AniListUser {

    fun userList(userID: String, token: String): String {
        val url = URL("https://graphql.anilist.co")
        var connection: HttpURLConnection? = null

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

        try {
            connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Authorization", "Bearer $token")

            val jsonObject = JSONObject()
            try {
                jsonObject.put("query", graph)
            } catch (e: Exception) {
                Log.e("TAG", "Error creating JSON object: ${e.message}")
                return "{}"
            }

            connection.outputStream.use { os ->
                val input = jsonObject.toString().toByteArray(StandardCharsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return connection.inputStream.bufferedReader().readText()
            } else {
                Log.e(
                    "TAG",
                    "HTTP Error Response Code: $responseCode: ${connection.responseMessage}"
                )
                return "{}"
            }
        } catch (ex: Exception) {
            Log.e("TAG", "Error connecting to AniList: ${ex.message}")
            return "{}"
        } finally {
            connection?.disconnect()
        }
    }

}