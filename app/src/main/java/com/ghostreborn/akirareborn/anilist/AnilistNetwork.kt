package com.ghostreborn.akirareborn.anilist

import com.ghostreborn.akirareborn.Constants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AnilistNetwork {
    private fun connectAnilist(graph:String): String? {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val json = JSONObject()
            .put("query", graph)
            .toString()
        val body: RequestBody = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://graphql.anilist.co")
            .post(body)
            .addHeader("Authorization", "Bearer ${Constants.anilistToken}")
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }

    fun getAnimeList(type: String, status: String): String? {
        val graph = "query{" +
                "  MediaListCollection(userId:${Constants.anilistUserID},type:${type}, status:${status}){" +
                "    lists {" +
                "      entries {" +
                "        media{" +
                "          idMal" +
                "        }" +
                "        progress" +
                "      }" +
                "    }" +
                "  }" +
                "}"
        return connectAnilist(graph)
    }
}