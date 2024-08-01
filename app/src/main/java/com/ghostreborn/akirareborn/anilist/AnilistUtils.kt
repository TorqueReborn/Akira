package com.ghostreborn.akirareborn.anilist

import android.content.Context
import androidx.room.Room
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.allanime.AllAnimeParser
import com.ghostreborn.akirareborn.database.AnilistUser
import com.ghostreborn.akirareborn.database.AnilistUserDatabase
import com.ghostreborn.akirareborn.model.Anilist
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AnilistUtils {

    fun getToken(code: String, context: Context) {
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
        Constants.preferences
            .edit()
            .putString(Constants.AKIRA_CODE, code)
            .putString(Constants.AKIRA_TOKEN, token)
            .apply()
        getUserNameAndID(token, context)
    }

    private fun getUserNameAndID(token: String, context: Context) {
        val client = OkHttpClient()
        val query = "{Viewer{id,name}}"
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val json = JSONObject()
            .put("query", query)
            .toString()
        val body: RequestBody = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://graphql.anilist.co")
            .post(body)
            .addHeader("Authorization", "Bearer $token")
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        val id = JSONObject(responseBody.toString()).getJSONObject("data").getJSONObject("Viewer")
            .getString("id")
        val name = JSONObject(responseBody.toString()).getJSONObject("data").getJSONObject("Viewer")
            .getString("name")
        Constants.preferences
            .edit()
            .putString(Constants.AKIRA_USER_ID, id)
            .putString(Constants.AKIRA_USER_NAME, name)
            .putBoolean(Constants.AKIRA_LOGGED_IN, true)
            .apply()
        Constants.anilistToken = token
        getAnilist(context)
    }

    private fun getAnimeList(): String? {
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
        return AnilistNetwork().connectAnilist(graph)
    }

    private fun getAnilist(context: Context) {
        val rawJSON = getAnimeList()
        val anilistAnimes: ArrayList<Anilist> = ArrayList()
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
            anilistAnimes.add(Anilist(id, malId, allAnimeId, title, progress))
        }
        val instance = Room.databaseBuilder(
            context,
            AnilistUserDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()
        for (i in 0 until anilistAnimes.size) {
            instance.anilistUserDao().insertAll(
                AnilistUser(
                    anilistAnimes[i].mediaId,
                    anilistAnimes[i].malId,
                    anilistAnimes[i].allAnimeID,
                    anilistAnimes[i].title,
                    anilistAnimes[i].progress
                )
            )
        }
    }

}