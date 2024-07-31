package com.ghostreborn.akirareborn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akirareborn.Constants.preferences
import com.ghostreborn.akirareborn.anilist.AnilistUtils
import com.ghostreborn.akirareborn.fragment.MainFragment
import com.ghostreborn.akirareborn.test.TestFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    // TODO show a sweepable tab layout for anilist for CURRENT, DROPPED etc
    // TODO save progress on episode playback is around 75%
    // TODO if anime is in Anilist, show a way to update progress
    // TODO if not available in Anilist, show add button
    // TODO add to Anilist based on category like CURRENT, DROPPED etc
    // TODO get anilist locally ie save it in database
    // TODO if saved in database, it is easy to call for non anilist checking
    // TODO show setting screen to change sub or dub

    // TODO IDEA - Login with Anilist upon app opening
    // TODO store list of entries in local database
    // TODO use the mediaListEntryID to save progress to cloud
    // TODO thus Anilist API is not spammed with requests
    // TODO use malId to get allAnimeId and there is an api to use array of
    //  allAnimeIds to get anime data


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getToken()
        replaceFragment()
    }

    private fun getToken() {
        preferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE)
        if (preferences.getBoolean(Constants.AKIRA_LOGGED_IN, false)) {
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

    private fun replaceFragment() {
        if (preferences.getBoolean(Constants.AKIRA_LOGGED_IN, false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_layout, TestFragment())
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_layout, MainFragment())
                .commit()
        }
    }

}