package com.ghostreborn.akira.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.room.Room
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.database.SavedEntryDatabase

class AkiraUtils {

    fun checkLogin(context: Context): Boolean {
        return context.getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
            .getBoolean(Constants.PREF_LOGGED_IN, false)
    }

    fun getDB(context: Context): SavedEntryDatabase {
        return Room.databaseBuilder(
            context,
            SavedEntryDatabase::class.java, "my-database"
        ).build()
    }

}