package com.ghostreborn.akirareborn.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.adapter.EpisodeAdapter
import com.ghostreborn.akirareborn.adapter.EpisodeGroupAdapter
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.fragment.AnimeFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episodes)

        val episodeRecycler: RecyclerView = findViewById(R.id.episodes_recycler_view)
        val episodeGroupRecycler: RecyclerView = findViewById(R.id.episode_group_recycler_view)

        CoroutineScope(Dispatchers.IO).launch {
            val group = AllAnimeParser().episodes(AnimeFragment.allAnimeID)
            val parsed = AllAnimeParser().episodeDetails(AnimeFragment.allAnimeID, group[0])
            withContext(Dispatchers.Main) {
                episodeRecycler.adapter = EpisodeAdapter(parsed, this@EpisodesActivity)
                episodeRecycler.layoutManager = LinearLayoutManager(this@EpisodesActivity)
                episodeGroupRecycler.adapter =
                    EpisodeGroupAdapter(this@EpisodesActivity, group, episodeRecycler)
                episodeGroupRecycler.layoutManager = LinearLayoutManager(
                    this@EpisodesActivity, LinearLayoutManager.HORIZONTAL,
                    false
                )
            }
        }

    }
}