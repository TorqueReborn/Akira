package com.ghostreborn.akira.parsers.anilist

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import com.ghostreborn.akira.Constants
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
    }
}