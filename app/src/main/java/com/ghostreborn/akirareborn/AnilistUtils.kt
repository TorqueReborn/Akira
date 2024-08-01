package com.ghostreborn.akirareborn

import android.content.Context
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
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
    }
}