package com.ghostreborn.akira.utils

import android.content.Context
import com.ghostreborn.akira.Constants

class AkiraUtils {

    fun checkLogin(context: Context):Boolean{
        return context
            .getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
            .getString(Constants.LOGIN_CODE, "") != ""
    }

    fun storeLoginCode(code: String, context:Context){
        context
            .getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE).edit()
            .putString(Constants.LOGIN_CODE, code)
            .apply()
    }

}