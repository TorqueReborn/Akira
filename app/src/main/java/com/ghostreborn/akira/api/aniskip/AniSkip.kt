package com.ghostreborn.akira.api.aniskip

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class AniSkip {

    private fun startSkip(id: String, episode: String): Map<String, Long> {
        val url = "https://api.aniskip.com/v2/skip-times/$id/$episode?types=op&episodeLength=0"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allmanga.to")
        return parseSkip(connection.inputStream.bufferedReader().readText(), "OP_")
    }

    private fun endSkip(id: String, episode: String): Map<String, Long> {
        val url =
            "https://api.aniskip.com/v2/skip-times/$id/$episode?types=ed&episodeLength=0"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Referer", "https://allmanga.to")
        return parseSkip(connection.inputStream.bufferedReader().readText(), "ED_")
    }

    private fun parseSkip(rawJSON: String, name: String): Map<String, Long> {
        val startSkip: MutableMap<String, Long> = HashMap()
        try {
            val interval = JSONObject(rawJSON)
                .getJSONArray("results")
                .getJSONObject(0)
                .getJSONObject("interval")
            val startTime: Long
            val startTimeStr = interval.getString("startTime")
            startTime = if (startTimeStr.contains(".")) {
                startTimeStr.replace(".", "").toLong()
            } else {
                (startTimeStr + "000").toLong()
            }

            val endTime: Long
            val endTimeStr = interval.getString("endTime")
            endTime = if (endTimeStr.contains(".")) {
                endTimeStr.replace(".", "").toLong()
            } else {
                (endTimeStr + "000").toLong()
            }
            startSkip[name + "startTime"] = startTime
            startSkip[name + "endTime"] = endTime
        } catch (e: JSONException) {
            Log.e("TAG", e.toString())
        }
        return startSkip
    }

    fun startEndSkip(id: String, episode: String): Map<String, Long> {
        val startEndSkips: MutableMap<String, Long> = HashMap()
        startEndSkips.putAll(startSkip(id, episode))
        startEndSkips.putAll(endSkip(id, episode))
        return startEndSkips
    }

}