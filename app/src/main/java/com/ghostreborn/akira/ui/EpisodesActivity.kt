package com.ghostreborn.akira.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.EpisodeAdapter
import com.ghostreborn.akira.parsers.AllAnime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episodes)

        val recycler = findViewById<RecyclerView>(R.id.episodes_recycler)

        CoroutineScope(Dispatchers.IO).launch {
            val episodes = AllAnime().episodes(Constants.animeId)[0]
            withContext(Dispatchers.Main) {
                recycler.adapter = EpisodeAdapter(episodes,supportFragmentManager)
                recycler.layoutManager = LinearLayoutManager(this@EpisodesActivity)
            }
        }

    }
}