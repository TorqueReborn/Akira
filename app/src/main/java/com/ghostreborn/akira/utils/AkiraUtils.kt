package com.ghostreborn.akira.utils

import android.content.Context
import com.ghostreborn.akira.Constants

class AkiraUtils {

    fun checkLogin(context: Context):Boolean{
        return context
            .getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
            .getBoolean(Constants.LOGGED_IN, false)
    }

}