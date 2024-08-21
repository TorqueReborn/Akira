package com.ghostreborn.akira.anilist

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.room.Room
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.MainActivity
import com.ghostreborn.akira.allAnime.AllAnimeParser
import com.ghostreborn.akira.allManga.AllMangaParser
import com.ghostreborn.akira.database.Anilist
import com.ghostreborn.akira.database.AnilistDatabase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AnilistUtils {

    fun getToken(code: String, activity: Activity) {
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val json = JSONObject().apply {
            put("grant_type", "authorization_code")
            put("client_id", "20149")
            put("client_secret", "taduUeXEUEZzKfQ5Av0VapX2aLhYgqSWEJgHt9uQ")
            put("redirect_uri", "wanpisu://ghostreborn.in")
            put("code", code)
        }.toString()

        val request = Request.Builder()
            .url("https://anilist.co/api/v2/oauth/token")
            .post(json.toRequestBody(mediaType))
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()

        OkHttpClient().newCall(request).execute().use { response ->
            val token = JSONObject(response.body?.string() ?: "").getString("access_token")
            activity.getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit()
                .putString(Constants.PREF_TOKEN, token).apply()
            getUserNameAndID(activity)
        }
    }

    private fun getUserNameAndID(activity: Activity) {
        val query = "{Viewer{id}}"
        val responseBody = AnilistNetwork().connectAnilist(query, activity)
        val id =
            JSONObject(responseBody).getJSONObject("data").getJSONObject("Viewer").getString("id")
        activity.getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit()
            .putString(Constants.PREF_USER_ID, id).apply()
        getAnimeList(activity)
    }

    private fun getAnimeList(activity: Activity) {
        val graph = "query{MediaListCollection(userId:${
            activity.getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).getString(
                Constants.PREF_USER_ID,
                ""
            )
        },type:ANIME, status:CURRENT){lists{entries{id,media{idMal,title{native}},progress}}}}"
        val response = JSONObject(AnilistNetwork().connectAnilist(graph, activity))
        val lists = response.getJSONObject("data").getJSONObject("MediaListCollection")
            .getJSONArray("lists")

        if (lists.length() == 0) {
            activity.getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit()
                .putBoolean(Constants.PREF_LOGGED_IN, true).apply()
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
            return
        }

        val entries = lists.getJSONObject(0).getJSONArray("entries")
        val anilistAnime = ArrayList<Anilist>()

        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val media = entry.getJSONObject("media")
            val title = media.getJSONObject("title").getString("native")
            anilistAnime.add(
                Anilist(
                    entry.getString("id"),
                    media.getString("idMal"),
                    AllAnimeParser().allAnimeIdWithMalId(title, media.getString("idMal")),
                    title,
                    entry.getString("progress"),
                    false
                )
            )
        }

        Room.databaseBuilder(activity, AnilistDatabase::class.java, "Akira").build().apply {
            anilistDao().insertAll(*anilistAnime.toTypedArray())
            close()
        }

        getMangaList(activity)
    }

    private fun getMangaList(activity: Activity) {
        val graph = "query{MediaListCollection(userId:${
            activity.getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).getString(
                Constants.PREF_USER_ID,
                ""
            )
        },type:MANGA, status:CURRENT){lists{entries{id,media{idMal,title{native}},progress}}}}"
        val response = JSONObject(AnilistNetwork().connectAnilist(graph, activity))
        val lists = response.getJSONObject("data").getJSONObject("MediaListCollection")
            .getJSONArray("lists")

        if (lists.length() == 0) {
            activity.getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit()
                .putBoolean(Constants.PREF_LOGGED_IN, true).apply()
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
            return
        }

        val entries = lists.getJSONObject(0).getJSONArray("entries")
        val anilistAnime = ArrayList<Anilist>()

        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val media = entry.getJSONObject("media")
            val title = media.getJSONObject("title").getString("native")
            anilistAnime.add(
                Anilist(
                    entry.getString("id"),
                    media.getString("idMal"),
                    AllMangaParser().allAnimeIdWithMalId(title, media.getString("idMal")),
                    title,
                    entry.getString("progress"),
                    true
                )
            )
        }

        Room.databaseBuilder(activity, AnilistDatabase::class.java, "Akira").build().apply {
            anilistDao().insertAll(*anilistAnime.toTypedArray())
            close()
        }

        activity.getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit()
            .putBoolean(Constants.PREF_LOGGED_IN, true).apply()
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }
}