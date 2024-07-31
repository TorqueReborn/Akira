package com.ghostreborn.akirareborn.test

import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.allanime.AllAnimeParser
import com.ghostreborn.akirareborn.model.Anilist
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class TestAPI {

    private fun connectAnilist(graph: String): String? {
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

    fun getAnimeList(): String? {
        val graph = "query{" +
                "  MediaListCollection(userId:6073083,type:ANIME, status:CURRENT){" +
                "    lists {" +
                "      entries {" +
                "        id" +
                "        media{" +
                "          idMal" +
                "          title{" +
                "            native" +
                "          }" +
                "        }" +
                "        progress" +
                "      }" +
                "    }" +
                "  }" +
                "}"
        return connectAnilist(graph)
    }

    fun getAnilist(): String {
        val rawJSON = getAnimeList()
        Constants.anilistTest = ArrayList()
        val entries = JSONObject(rawJSON.toString())
            .getJSONObject("data")
            .getJSONObject("MediaListCollection")
            .getJSONArray("lists")
            .getJSONObject(0)
            .getJSONArray("entries")
        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val id = entry.getString("id")
            val malId = entry.getJSONObject("media").getString("idMal")
            val title = entry.getJSONObject("media").getJSONObject("title").getString("native")
            val progress = entry.getString("progress")
            val allAnimeId = AllAnimeParser().allAnimeIdWithMalId(title, malId)
            Constants.anilistTest.add(Anilist(id, malId, allAnimeId, title, progress))
        }
        return entries.toString()
    }

}