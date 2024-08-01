package com.ghostreborn.akirareborn.anilist

import android.app.Activity
import android.content.Intent
import androidx.room.Room
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.MainActivity
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.database.Anilist
import com.ghostreborn.akirareborn.database.AnilistDatabase
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
            Constants.preferences.edit().putString(Constants.PREF_TOKEN, token).apply()
            getUserNameAndID(activity)
        }
    }

    private fun getUserNameAndID(activity: Activity) {
        val query = "{Viewer{id}}"
        val responseBody = AnilistNetwork().connectAnilist(query)
        val id =
            JSONObject(responseBody).getJSONObject("data").getJSONObject("Viewer").getString("id")
        Constants.preferences.edit().putString(Constants.PREF_USER_ID, id).apply()
        getAnimeList(activity)
    }

    private fun getAnimeList(activity: Activity) {
        val graph = "query{MediaListCollection(userId:${
            Constants.preferences.getString(
                Constants.PREF_USER_ID,
                ""
            )
        },type:ANIME, status:CURRENT){lists{entries{id,media{idMal,title{native}},progress}}}}"
        val response = JSONObject(AnilistNetwork().connectAnilist(graph))
        val lists = response.getJSONObject("data").getJSONObject("MediaListCollection")
            .getJSONArray("lists")

        if (lists.length() == 0) {
            Constants.preferences.edit().putBoolean(Constants.PREF_LOGGED_IN, true).apply()
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
                    entry.getString("progress")
                )
            )
        }

        Room.databaseBuilder(activity, AnilistDatabase::class.java, "Akira").build().apply {
            anilistDao().insertAll(*anilistAnime.toTypedArray())
            close()
        }

        Constants.preferences.edit().putBoolean(Constants.PREF_LOGGED_IN, true).apply()
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }
}