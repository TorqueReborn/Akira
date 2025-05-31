package com.ghostreborn.akira.api.anilList

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class AniListAuthorize {

    fun getToken(code: String): String {
        val jsonPayload = JSONObject().apply {
            put("grant_type", "authorization_code")
            put("client_id", "25543")
            put("client_secret", "r4tJrIO1c4LwmGLch6F84nkfAFKk9lxvR8hezcaf")
            put("redirect_uri", "akira://ghostreborn.in")
            put("code", code)
        }
        val url = URL("https://anilist.co/api/v2/oauth/token")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true

        connection.outputStream.use { os ->
            val input = jsonPayload.toString().toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)
        }

        val rawJSON = connection.inputStream.bufferedReader().readText()
        return JSONObject(rawJSON)
            .getString("access_token")
    }

    fun getUserID(token: String): String {
        val query = "{Viewer{id}}"
        val jsonPayload = JSONObject().apply {
            put("query", query)
        }

        val url = URL("https://graphql.anilist.co")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.doOutput = true

        connection.outputStream.use { os ->
            val input = jsonPayload.toString().toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)
        }

        val rawJSON = connection.inputStream.bufferedReader().readText()
        return JSONObject(rawJSON)
            .getJSONObject("data")
            .getJSONObject("Viewer")
            .getString("id")
    }

}