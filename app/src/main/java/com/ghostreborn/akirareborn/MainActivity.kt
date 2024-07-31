package com.ghostreborn.akirareborn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.ghostreborn.akirareborn.adapter.MainViewPagerAdapter
import com.ghostreborn.akirareborn.anilist.AnilistUtils
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setData()
        getToken()
        setViewPager()
    }

    private fun setViewPager() {
        val viewPager: ViewPager2 = findViewById(R.id.main_view_pager)
        viewPager.adapter = MainViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.currentItem = 1
    }

    private fun setData() {
        Constants.akiraSharedPreferences =
            getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE)
        Constants.anilistToken =
            Constants.akiraSharedPreferences.getString(Constants.AKIRA_TOKEN, "")!!
        Constants.anilistUserID =
            Constants.akiraSharedPreferences.getString(Constants.AKIRA_USER_ID, "")!!
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