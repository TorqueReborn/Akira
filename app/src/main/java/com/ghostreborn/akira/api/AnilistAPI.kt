package com.ghostreborn.akira.api

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AnilistAPI {
    fun details(): String {

        val query = """{
          "query": "query { Media(id: 21, type: ANIME) { title { userPreferred } nextAiringEpisode { episode airingAt timeUntilAiring } } }"
        }""".trimIndent()

        val request = Request.Builder()
            .url("https://graphql.anilist.co")
            .post(query.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
            .addHeader("Content-Type", "application/json")
            .build()

        return OkHttpClient().newCall(request).execute().body?.string().toString()


    }
}