package com.ghostreborn.akira.utils

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AkiraUtils {

    fun checkLogin(context: Context): Boolean {
        return context
            .getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
            .getString(Constants.PREF_USER_ID, "") != ""
    }

    fun getUserID(context: Context): String {
        return context
            .getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
            .getString(Constants.PREF_USER_ID, "")!!
    }

    fun getTokenAndUserID(code: String, activity: Activity) {
        val client = OkHttpClient()
        val token = client.newCall(Request.Builder()
            .url("https://anilist.co/api/v2/oauth/token")
            .post("""{"grant_type":"authorization_code","client_id":"20149","client_secret":"taduUeXEUEZzKfQ5Av0VapX2aLhYgqSWEJgHt9uQ","redirect_uri":"wanpisu://ghostreborn.in","code":"$code"}""".toRequestBody("application/json".toMediaType()))
            .build()
        ).execute().use { JSONObject(it.body!!.string()).getString("access_token") }

        val userId = client.newCall(Request.Builder()
            .url("https://graphql.anilist.co")
            .post(JSONObject().put("query", "{Viewer{id}}").toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .addHeader("Authorization", "Bearer $token")
            .build()
        ).execute().use { JSONObject(it.body!!.string()).getJSONObject("data").getJSONObject("Viewer").getString("id") }

        activity.getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).edit()
            .putString(Constants.PREF_USER_ID, userId).apply()
        activity.startActivity(Intent(activity, MainActivity::class.java)).also { activity.finish() }
    }

}