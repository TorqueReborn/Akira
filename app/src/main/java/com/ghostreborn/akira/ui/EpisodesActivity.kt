package com.ghostreborn.akira.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.R
import com.ghostreborn.akira.api.allAnime.AllAnime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episodes)

        CoroutineScope(Dispatchers.IO).launch {
            val test = AllAnime().allAnimeID("One Piece", "21")
            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.test_text).text = test.toString()
            }
        }

    }
}