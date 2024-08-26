package com.ghostreborn.akira.parsers.anilist

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.Manga
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AnilistNetwork {
    fun connectAnilist(graph: String, activity: Activity): String {
        val body = JSONObject().apply {
            put("query", graph)
        }.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://graphql.anilist.co")
            .post(body)
            .addHeader(
                "Authorization",
                "Bearer ${activity.getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).getString(Constants.PREF_TOKEN, "")}"
            )
            .build()
        return OkHttpClient().newCall(request).execute().body!!.string()
    }

    fun getAnimeList(activity: Activity):ArrayList<Anime> {
        val userID = activity.getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).getString(Constants.PREF_USER_ID, "")
        val graph = "query{MediaListCollection(userId:${userID},type:ANIME, status:CURRENT){lists{entries{media{id,title{userPreferred}coverImage{medium}}}}}}"
        val response = JSONObject(AnilistNetwork().connectAnilist(graph, activity))
        val lists = response.getJSONObject("data").getJSONObject("MediaListCollection")
            .getJSONArray("lists")

        if (lists.length() == 0) {
            return ArrayList()
        }

        val entries = lists.getJSONObject(0).getJSONArray("entries")
        val anilistAnime = ArrayList<Anime>()

        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val media = entry.getJSONObject("media")
            anilistAnime.add(
                Anime(
                    media.getString("id"),
                    media.getJSONObject("title").getString("userPreferred"),
                    media.getJSONObject("coverImage").getString("medium")
                )
            )
        }

        return anilistAnime
    }

    fun getMangaList(activity: Activity):ArrayList<Manga> {
        val userID = activity.getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).getString(Constants.PREF_USER_ID, "")
        val graph = "query{MediaListCollection(userId:${userID},type:MANGA, status:CURRENT){lists{entries{media{id,title{userPreferred}coverImage{medium}}}}}}"
        val response = JSONObject(AnilistNetwork().connectAnilist(graph, activity))
        val lists = response.getJSONObject("data").getJSONObject("MediaListCollection")
            .getJSONArray("lists")

        if (lists.length() == 0) {
            return ArrayList()
        }

        val entries = lists.getJSONObject(0).getJSONArray("entries")
        val anilistManga = ArrayList<Manga>()

        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val media = entry.getJSONObject("media")
            anilistManga.add(
                Manga(
                    media.getString("id"),
                    media.getJSONObject("title").getString("userPreferred"),
                    media.getJSONObject("coverImage").getString("medium")
                )
            )
        }

        return anilistManga
    }
}