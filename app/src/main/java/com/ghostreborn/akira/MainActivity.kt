package com.ghostreborn.akira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.fragment.AnimeFragment
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

        supportFragmentManager.beginTransaction()
            .replace(R.id.anilist_frame, AnimeFragment(false))
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