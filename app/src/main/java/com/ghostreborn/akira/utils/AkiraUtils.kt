package com.ghostreborn.akira.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.models.retro.EntryMinimized
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

    suspend fun ids(context: Context): List<String> {
        val userID = getUserID(context)
        val entryMinimized = ArrayList<EntryMinimized>()
        KitsuAPI().ids(userID, 0)?.meta?.count?.let { total ->
            for (i in 0 until total step 50) {
                entryMinimized.add(KitsuAPI().ids(userID, i)!!)
            }
        }
        return entryMinimized.flatMap { it.data.map { entry -> entry.id } }
    }
}