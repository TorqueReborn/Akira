package com.ghostreborn.akirareborn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akirareborn.anilist.AnilistUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // TODO show progress based on episode titles parsed
    // TODO login to Anilist only on Anilist Fragment
    // TODO show a button to login to Anilist in AnilistFragment
    // TODO convert AnimeDetailsActivity as fragment for anilist and allAnime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setData()
        getToken()
    }

    private fun setData() {
        Constants.akiraSharedPreferences =
            getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE)
        Constants.anilistToken = Constants.akiraSharedPreferences.getString(Constants.AKIRA_TOKEN, "")!!
        Constants.anilistUserID = Constants.akiraSharedPreferences.getString(Constants.AKIRA_USER_ID, "")!!
        Log.e("TAG", Constants.anilistUserID)
    }

    private fun getToken() {
        if (Constants.akiraSharedPreferences.getBoolean(Constants.AKIRA_LOGGED_IN, false)) {
            return
        }
        val intent: Intent = intent
        val uri = intent.data
        if (uri != null) {
            val code = uri.getQueryParameter("code")
            CoroutineScope(Dispatchers.IO).launch {
                AnilistUtils().getToken(code!!)
            }
        }
    }

}