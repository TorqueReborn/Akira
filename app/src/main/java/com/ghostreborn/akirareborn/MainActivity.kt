package com.ghostreborn.akirareborn

import android.content.Intent
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

        setData()
        getData()
        setViewPager()
    }

    private fun setViewPager() {
        val viewPager: ViewPager2 = findViewById(R.id.anime_view_pager)
        viewPager.adapter = AnimeViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.currentItem = 0
    }

    private fun setData() {
        Constants.preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
    }

    private fun getData() {
        val intent: Intent = intent
        val uri = intent.data
        if (uri != null) {
            val code = uri.getQueryParameter("code").toString()
            CoroutineScope(Dispatchers.IO).launch {
                AnilistUtils().getToken(code, this@MainActivity)
            }
        }
    }
}