package com.ghostreborn.akira.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.models.Anime
import com.ghostreborn.akira.parser.kitsu.KitsuAPI

class AkiraUtils {
    fun checkLogin(context: Context): Boolean {
        return context
            .getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
            .getString(Constants.PREF_USER_ID, "") != ""
    }

    private fun getUserID(context: Context): String {
        return context
            .getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
            .getString(Constants.PREF_USER_ID, "")!!
    }

    suspend fun entry(context: Context): ArrayList<Anime> {
        val userID = getUserID(context)
        val entry = KitsuAPI().entry(userID, Constants.offset)
        if (entry?.links?.next != null) {
            Constants.offset += 10
        }else{
            return ArrayList()
        }
        val anime = ArrayList<Anime>()
        for (i in 0 until entry.data.size) {
            anime.add(
                Anime(
                    kitsuID = entry.data[i].id,
                    title = entry.included[i].attributes.canonicalTitle,
                    progress = entry.data[i].attributes.progress,
                    thumbnail = entry.included[i].attributes.posterImage.medium
                )
            )
        }
        return anime
    }
}