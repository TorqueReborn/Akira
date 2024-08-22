package com.ghostreborn.akira.utils

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class AkiraUtils {
    // Scrape assets -> node_id
    fun getLatestPackage() {
        val client = OkHttpClient()
        val url = "https://api.github.com/repos/TorqueReborn/Akira/releases/latest"
        val request = Request.Builder()
            .url(url)
            .build()
        val raw = client.newCall(request).execute().body?.string().toString()
        val json = JSONObject(raw).getJSONArray("assets")
            .getJSONObject(0)
            .getString("node_id")
        Log.e("TAG", json)
    }
}