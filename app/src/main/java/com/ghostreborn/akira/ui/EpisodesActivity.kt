package com.ghostreborn.akira.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.EpisodeAdapter
import com.ghostreborn.akira.adapter.EpisodeGroupAdapter
import com.ghostreborn.akira.parsers.AllAnime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episodes)

        val episodesRecycler = findViewById<RecyclerView>(R.id.episodes_recycler_view)
        val episodesGroupRecycler = findViewById<RecyclerView>(R.id.episode_group_recycler_view)

        CoroutineScope(Dispatchers.IO).launch {
            val episodes = AllAnime().episodes(Constants.animeId)
            withContext(Dispatchers.Main) {
                episodesRecycler.adapter = EpisodeAdapter(episodes[0],this@EpisodesActivity)
                episodesRecycler.layoutManager = LinearLayoutManager(this@EpisodesActivity)
                episodesGroupRecycler.adapter = EpisodeGroupAdapter(
                    this@EpisodesActivity, episodes, episodesRecycler
                )
                episodesGroupRecycler.layoutManager = LinearLayoutManager(
                    this@EpisodesActivity, LinearLayoutManager.HORIZONTAL, false
                )
            }
        }

    }
}