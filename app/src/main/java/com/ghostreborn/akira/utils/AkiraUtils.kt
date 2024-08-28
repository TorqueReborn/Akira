package com.ghostreborn.akira.utils

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AkiraUtils {

    fun checkLogin(context: Context): Boolean {
        return context
            .getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
            .getString(Constants.PREF_TOKEN, "") != ""
    }

    fun getToken(code: String, activity: Activity) {
        val token = OkHttpClient().newCall(
            Request.Builder()
                .url("https://anilist.co/api/v2/oauth/token")
                .post("""
                    {"grant_type":"authorization_code",
                    "client_id":"20149",
                     "client_secret":"taduUeXEUEZzKfQ5Av0VapX2aLhYgqSWEJgHt9uQ",
                     "redirect_uri":"wanpisu://ghostreborn.in",
                     "code":"$code"}"""
                    .trimMargin().toRequestBody("application/json".toMediaType())
                ).build()
        ).execute().use { JSONObject(it.body?.string().toString()).getString("access_token") }

        activity.getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).edit()
            .putString(Constants.PREF_TOKEN, token).apply()
        activity.startActivity(Intent(activity, MainActivity::class.java)).apply { activity.finish() }
    }

}