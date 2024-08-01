package com.ghostreborn.akirareborn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akirareborn.Constants.PREF_NAME
import com.ghostreborn.akirareborn.anilist.AnilistUtils
import com.ghostreborn.akirareborn.fragment.AnilistLoginFragment
import com.ghostreborn.akirareborn.fragment.AnimeFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setData()
        getData()
        setFragment()
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
                AnilistUtils().getToken(code, baseContext)
            }
        }
    }

    private fun setFragment() {
        if (Constants.preferences.getBoolean(Constants.PREF_LOGGED_IN, false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_layout, AnimeFragment())
                .commit()
        } else{
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_layout, AnilistLoginFragment())
                .commit()
        }
    }
}