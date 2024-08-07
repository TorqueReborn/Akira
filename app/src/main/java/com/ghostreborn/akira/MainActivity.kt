package com.ghostreborn.akira

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.ghostreborn.akira.Constants.PREF_NAME
import com.ghostreborn.akira.adapter.AnimeViewPagerAdapter
import com.ghostreborn.akira.anilist.AnilistUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setup()
    }

    private fun setup() {
        Constants.preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        Log.e("TAG", Constants.preferences.getString(Constants.PREF_USER_ID, "").toString())
        setupViewPager()
        handleIntentData()
    }

    private fun setupViewPager() {
        val tabLayout = findViewById<TabLayout>(R.id.main_tabs_layout)
        val viewPager = findViewById<ViewPager2>(R.id.anime_view_pager)
        viewPager.adapter = AnimeViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.currentItem = 1
        val tabTitles = listOf("Anilist", "Home", "Settings")
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun handleIntentData() {
        intent.data?.getQueryParameter("code")?.let { code ->
            CoroutineScope(Dispatchers.IO).launch {
                AnilistUtils().getToken(code, this@MainActivity)
            }
        }
    }
}