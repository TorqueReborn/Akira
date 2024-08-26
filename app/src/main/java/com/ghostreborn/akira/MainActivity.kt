package com.ghostreborn.akira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.fragment.AnilistAnimeFragment
import com.ghostreborn.akira.fragment.AnilistLoginFragment
import com.ghostreborn.akira.fragment.AnilistMangaFragment
import com.ghostreborn.akira.fragment.AnimeFragment
import com.ghostreborn.akira.fragment.MangaFragment
import com.ghostreborn.akira.parsers.anilist.AnilistUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        handleIntentData()

        val isLoggedIn = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
            .getBoolean(Constants.PREF_LOGGED_IN, false)
        if (!isLoggedIn) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.anilist_login_frame, AnilistLoginFragment())
                .commit()
        }else{
            supportFragmentManager.beginTransaction()
                .replace(R.id.anilist_anime_frame, AnilistAnimeFragment(false))
                .replace(R.id.anilist_manga_frame, AnilistMangaFragment(false))
                .commit()
        }

        handleIntentData()
        supportFragmentManager.beginTransaction()
            .replace(R.id.home_anime_frame, AnimeFragment(false))
            .replace(R.id.home_manga_frame, MangaFragment(false))
            .commit()
    }

    private fun handleIntentData() {
        intent.data?.getQueryParameter("code")?.let { code ->
            CoroutineScope(Dispatchers.IO).launch {
                AnilistUtils().getToken(code, this@MainActivity)
            }
        }
    }

}