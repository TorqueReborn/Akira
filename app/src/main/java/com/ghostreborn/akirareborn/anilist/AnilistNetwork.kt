package com.ghostreborn.akirareborn.anilist

import com.ghostreborn.akirareborn.Constants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AnilistNetwork {
    private fun connectAnilist(graph: String): String? {
        val token = Constants.preferences.getString(Constants.PREF_TOKEN, "")
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val json = JSONObject()
            .put("query", graph)
            .toString()
        val body: RequestBody = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://graphql.anilist.co")
            .post(body)
            .addHeader("Authorization", "Bearer ${token}")
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }

    fun saveAnime(animeId: String, status: String, progress: String): String {
        val query = "mutation{" +
                "  SaveMediaListEntry(mediaId:$animeId,status:$status, progress:$progress) {" +
                "    id" +
                "    media{" +
                "      idMal" +
                "      title{" +
                "        native" +
                "      }" +
                "    }" +
                "    progress" +
                "  }" +
                "}"
        return connectAnilist(query).toString()
    }

    fun deleteAnime(mediaId: String): String {
        val query = "mutation {" +
                "  DeleteMediaListEntry(id: $mediaId) {" +
                "    deleted" +
                "  }" +
                "}"
        return connectAnilist(query).toString()
    }
}