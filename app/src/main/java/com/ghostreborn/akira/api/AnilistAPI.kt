package com.ghostreborn.akira.api

import com.ghostreborn.akira.models.AnimeDetails
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AnilistAPI {
    suspend fun details(id: String): AnimeDetails {

        val anilistID = KitsuAPI().mapping(id)?.data?.get(0)?.attributes?.externalId

        val query = """
        {
          "query": "query { Media(id: $anilistID, type: ANIME) { episodes title { userPreferred } description studios { nodes { name } } nextAiringEpisode { episode airingAt timeUntilAiring } relations { edges { relationType node { title { userPreferred } id type } } } } }"
        }
    """.trimIndent()

        val request = Request.Builder()
            .url("https://graphql.anilist.co")
            .post(query.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
            .addHeader("Content-Type", "application/json")
            .build()

        val rawJSON = OkHttpClient().newCall(request).execute().body?.string().toString()

        val media = JSONObject(rawJSON).getJSONObject("data").getJSONObject("Media")
        val title = media.getJSONObject("title").getString("userPreferred")
        val desc = media.getString("description")
        val studio = media.getJSONObject("studios").getJSONArray("nodes").getJSONObject(0).getString("name")

        val episode: String
        var airingAt = ""
        var timeUntilAiring = ""

        if (!media.isNull("nextAiringEpisode")){
            val nextEpisode = media.getJSONObject("nextAiringEpisode")
            episode = nextEpisode.getString("episode")
            airingAt = nextEpisode.getString("airingAt")
            timeUntilAiring = nextEpisode.getString("timeUntilAiring")
        }else{
            episode = media.getString("episodes")
        }

        val relations = media.getJSONObject("relations").getJSONArray("edges")
        var prequel = ""
        var sequel = ""
        for (i in 0 until relations.length()) {
            val relation = relations.getJSONObject(i)
            if (relation.getString("relationType") == "PREQUEL") {
                prequel = relation.getJSONObject("node").getString("id")
            }
            if (relation.getString("relationType") == "SEQUEL") {
                sequel = relation.getJSONObject("node").getString("id")
            }
        }

        return AnimeDetails(
            title,
            desc,
            studio,
            episode,
            airingAt,
            timeUntilAiring,
            prequel,
            sequel
        )

    }
}