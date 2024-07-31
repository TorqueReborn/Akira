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