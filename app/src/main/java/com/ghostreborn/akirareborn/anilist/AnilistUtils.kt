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
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AnilistUtils {

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

    fun getToken(code: String, activity: Activity) {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val json = JSONObject()
            .put("grant_type", "authorization_code")
            .put("client_id", "20149")
            .put("client_secret", "taduUeXEUEZzKfQ5Av0VapX2aLhYgqSWEJgHt9uQ")
            .put("redirect_uri", "wanpisu://ghostreborn.in")
            .put("code", code)
            .toString()
        val requestBody = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://anilist.co/api/v2/oauth/token")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        val token = JSONObject(responseBody.toString()).getString("access_token")
        Constants.preferences.edit().putString(Constants.PREF_TOKEN, token).apply()
        getUserNameAndID(activity)
    }

    private fun getUserNameAndID(activity: Activity) {
        val query = "{Viewer{id}}"
        val responseBody = connectAnilist(query)
        val id = JSONObject(responseBody.toString()).getJSONObject("data").getJSONObject("Viewer")
            .getString("id")
        Constants.preferences.edit().putString(Constants.PREF_USER_ID, id).apply()
        getAnimeList(activity)
    }

    private fun getAnimeList(activity: Activity) {
        val userID = Constants.preferences.getString(Constants.PREF_USER_ID, "")
        val graph = "query{" +
                "  MediaListCollection(userId:${userID},type:ANIME, status:CURRENT){" +
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
        val rawJSON = connectAnilist(graph)
        val anilistAnime: ArrayList<Anilist> = ArrayList()

        val lists = JSONObject(rawJSON.toString())
            .getJSONObject("data")
            .getJSONObject("MediaListCollection")
            .getJSONArray("lists")

        if (lists.length() == 0) {
            Constants.preferences.edit().putBoolean(Constants.PREF_LOGGED_IN, true).apply()
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
            return
        }

        val entries = lists
            .getJSONObject(0)
            .getJSONArray("entries")

        for (i in 0 until entries.length()) {
            val entry = entries.getJSONObject(i)
            val id = entry.getString("id")
            val malId = entry.getJSONObject("media").getString("idMal")
            val title = entry.getJSONObject("media").getJSONObject("title").getString("native")
            val progress = entry.getString("progress")
            val allAnimeId = AllAnimeParser().allAnimeIdWithMalId(title, malId)
            anilistAnime.add(Anilist(id, malId, allAnimeId, title, progress))
        }
        val instance = Room.databaseBuilder(
            activity,
            AnilistDatabase::class.java,
            "Akira"
        ).build()
        for (i in 0 until anilistAnime.size) {
            instance.anilistDao().insertAll(
                Anilist(
                    anilistAnime[i].id,
                    anilistAnime[i].malID,
                    anilistAnime[i].allAnimeID,
                    anilistAnime[i].title,
                    anilistAnime[i].progress
                )
            )
        }
        instance.close()
        Constants.preferences.edit().putBoolean(Constants.PREF_LOGGED_IN, true).apply()
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }
}