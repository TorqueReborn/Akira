package com.ghostreborn.akira.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.ghostreborn.akira.Constants

class AkiraUtils {

    fun checkLogin(context: Context): Boolean {
        return context.getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
            .getBoolean(Constants.PREF_LOGGED_IN, false)
    }

}