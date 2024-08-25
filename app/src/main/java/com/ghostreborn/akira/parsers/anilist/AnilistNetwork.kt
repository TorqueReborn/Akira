package com.ghostreborn.akira.parsers.anilist

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
}