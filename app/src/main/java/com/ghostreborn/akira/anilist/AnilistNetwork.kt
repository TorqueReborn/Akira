package com.ghostreborn.akira.anilist

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import com.ghostreborn.akira.Constants
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

    fun saveAnimeManga(animeId: String, status: String, progress: String, activity: Activity): String {
        val query = "mutation{SaveMediaListEntry(mediaId:$animeId,status:$status,progress:$progress){id,media{idMal,title{native}},progress}}"
        return connectAnilist(query, activity)
    }

    fun deleteAnimeManga(mediaId: String, activity: Activity): String {
        val query = "mutation{DeleteMediaListEntry(id:$mediaId){deleted}}"
        return connectAnilist(query, activity)
    }
}