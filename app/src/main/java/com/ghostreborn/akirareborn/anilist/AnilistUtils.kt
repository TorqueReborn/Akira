package com.ghostreborn.akirareborn.anilist

import com.ghostreborn.akirareborn.Constants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AnilistUtils {

    fun getToken(code: String) {
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
        Constants.akiraSharedPreferences
            .edit()
            .putString(Constants.AKIRA_CODE, code)
            .putString(Constants.AKIRA_TOKEN, token)
            .apply()
        getUserNameAndID(token)
    }

    private fun getUserNameAndID(token: String) {
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
        Constants.akiraSharedPreferences
            .edit()
            .putString(Constants.AKIRA_USER_ID, id)
            .putString(Constants.AKIRA_USERNAME, name)
            .putBoolean(Constants.AKIRA_LOGGED_IN, true)
            .apply()
    }

}