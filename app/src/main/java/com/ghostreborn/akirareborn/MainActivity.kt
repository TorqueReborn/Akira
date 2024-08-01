package com.ghostreborn.akirareborn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.ghostreborn.akirareborn.Constants.PREF_NAME
import com.ghostreborn.akirareborn.adapter.AnimeViewPagerAdapter
import com.ghostreborn.akirareborn.anilist.AnilistUtils
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
        setupViewPager()
        handleIntentData()
    }

    private fun setupViewPager() {
        findViewById<ViewPager2>(R.id.anime_view_pager).apply {
            adapter = AnimeViewPagerAdapter(supportFragmentManager, lifecycle)
            currentItem = 1
        }
    }

    private fun handleIntentData() {
        intent.data?.getQueryParameter("code")?.let { code ->
            CoroutineScope(Dispatchers.IO).launch {
                AnilistUtils().getToken(code, this@MainActivity)
            }
        }
    }
}