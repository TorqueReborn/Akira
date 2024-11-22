package com.ghostreborn.akira.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.EpisodeAdapter
import com.ghostreborn.akira.api.allAnime.AllAnimeParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episodes)

        val recycler = findViewById<RecyclerView>(R.id.episode_recycler)
        CoroutineScope(Dispatchers.IO).launch {

            Log.e("TAG", Constants.animeID)
            Log.e("TAG", Constants.animeName)

            val episodes = AllAnimeParser().episodes(Constants.animeName, Constants.animeID)
            withContext(Dispatchers.Main) {
                recycler.adapter = EpisodeAdapter(episodes[0])
                recycler.layoutManager = GridLayoutManager(this@EpisodesActivity, 3)
            }
        }

    }
}