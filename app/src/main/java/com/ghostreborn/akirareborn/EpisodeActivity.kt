package com.ghostreborn.akirareborn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        CoroutineScope(Dispatchers.IO).launch {
            AllAnimeParser().episodes("ReooPAxPMsHM4KPMY")
            withContext(Dispatchers.Main) {
                episodeRecycler.adapter = EpisodeAdapter(Constants.groupedEpisodes[0])
                episodeRecycler.layoutManager = LinearLayoutManager(this@EpisodeActivity)
                episodeGroupRecycler.adapter = EpisodeGroupAdapter(episodeRecycler)
                episodeGroupRecycler.layoutManager = LinearLayoutManager(this@EpisodeActivity, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }
}