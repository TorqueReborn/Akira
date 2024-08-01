package com.ghostreborn.akirareborn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akirareborn.anilist.AnilistUtils
import com.ghostreborn.akirareborn.fragment.AllAnimeFragment
import com.ghostreborn.akirareborn.fragment.MainFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    // TODO show a sweepable tab layout for anilist for CURRENT, DROPPED etc
    // TODO save progress on episode playback is around 75%
    // TODO if anime is in Anilist, show a way to update progress
    // TODO if not available in Anilist, show add button
    // TODO add to Anilist based on category like CURRENT, DROPPED etc
    // TODO show setting screen to change sub or dub

    // TODO use the mediaListEntryID to save progress to cloud
    // TODO thus Anilist API is not spammed with requests
    // TODO use malId to get allAnimeId and there is an api to use array of
    //  allAnimeIds to get anime data

    // TODO make progress saving as a separate fragment and attach it
    //  to AllAnimeDetailsFragment when database has allAnimeID use
    //  frameLayout in AllAnimeDetailsFragment to do so

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getToken()
        replaceFragment()
    }

    private fun getToken() {
        val intent: Intent = intent
        val uri = intent.data
        if (uri != null) {
            val code = uri.getQueryParameter("code")
            CoroutineScope(Dispatchers.IO).launch {
                AnilistUtils().getToken(code!!, baseContext)
            }
        }
    }

    private fun replaceFragment() {

    }

}