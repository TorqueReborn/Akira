package com.ghostreborn.akirareborn.ui

import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.adapter.EpisodeAdapter
import com.ghostreborn.akirareborn.adapter.EpisodeGroupAdapter
import com.ghostreborn.akirareborn.allanime.AllAnimeParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode)

        val episodeRecycler: RecyclerView = findViewById(R.id.episode_recycler_view)
        val episodeGroupRecycler: RecyclerView = findViewById(R.id.episode_group_recycler_view)
        val episodeProgressBar: ProgressBar = findViewById(R.id.episode_progress_bar)

        CoroutineScope(Dispatchers.IO).launch {
            AllAnimeParser().episodes(Constants.allAnimeID)
            withContext(Dispatchers.Main) {
                episodeProgressBar.visibility = ProgressBar.GONE
                episodeRecycler.adapter = EpisodeAdapter(this@EpisodeActivity)
                episodeRecycler.layoutManager = LinearLayoutManager(this@EpisodeActivity)
                if (Constants.groupedEpisodes.size > 1) {
                    episodeGroupRecycler.adapter =
                        EpisodeGroupAdapter(episodeRecycler, this@EpisodeActivity, episodeProgressBar)
                    episodeGroupRecycler.layoutManager = LinearLayoutManager(
                        this@EpisodeActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                }
            }
        }
    }
}