package com.ghostreborn.akira.ui

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.EpisodeAdapter
import com.ghostreborn.akira.adapter.EpisodeGroupAdapter
import com.ghostreborn.akira.allAnime.AllAnimeParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodesActivity : AppCompatActivity() {
    private lateinit var episodeRecycler: RecyclerView
    private lateinit var episodeGroupRecycler: RecyclerView
    private lateinit var episodeProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episodes)

        episodeRecycler = findViewById(R.id.episodes_recycler_view)
        episodeGroupRecycler = findViewById(R.id.episode_group_recycler_view)
        episodeProgressBar = findViewById(R.id.episode_progress_bar)

        fetchEpisodes()
    }

    private fun fetchEpisodes() {
        CoroutineScope(Dispatchers.IO).launch {
            val group = AllAnimeParser().episodes(Constants.allAnimeID)
            withContext(Dispatchers.Main) {
                if (group.isNotEmpty()) {
                    setupRecyclerViews(group)
                } else {
                    showNoEpisodesToast()
                }
            }
        }
    }

    private fun setupRecyclerViews(group: ArrayList<ArrayList<String>>) {
        CoroutineScope(Dispatchers.IO).launch {
            val parsed = AllAnimeParser().episodeDetails(
                Constants.allAnimeID, group[getGroup(Constants.animeEpisode, group)]
            )
            withContext(Dispatchers.Main) {
                episodeProgressBar.visibility = ProgressBar.GONE
                episodeRecycler.adapter = EpisodeAdapter(parsed, this@EpisodesActivity)
                episodeRecycler.layoutManager = LinearLayoutManager(this@EpisodesActivity)
                episodeGroupRecycler.adapter = EpisodeGroupAdapter(
                    this@EpisodesActivity, group, episodeRecycler, episodeProgressBar
                )
                episodeGroupRecycler.layoutManager = LinearLayoutManager(
                    this@EpisodesActivity, LinearLayoutManager.HORIZONTAL, false
                )
            }
        }
    }

    private fun showNoEpisodesToast() {
        Toast.makeText(this, "No episodes found", Toast.LENGTH_SHORT).show()
    }

    private fun getGroup(find: String, group: ArrayList<ArrayList<String>>): Int {
        return group.indexOfFirst { innerList -> innerList.contains(find) }.takeIf { it >= 0 } ?: 0
    }
}