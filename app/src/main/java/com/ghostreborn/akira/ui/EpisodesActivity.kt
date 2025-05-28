package com.ghostreborn.akira.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.EpisodeAdapter

class EpisodesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_episodes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val episodesRecycler = findViewById<RecyclerView>(R.id.episodes_recycler)
        episodesRecycler.layoutManager = GridLayoutManager(this, 5, LinearLayoutManager.VERTICAL, false)

        if(intent != null) {
            val episodes = intent.getStringArrayListExtra("animeEpisodes")
            val reversedEpisodes: ArrayList<String> = ArrayList()
            for(i in episodes!!.size - 1 downTo 0) {
                reversedEpisodes.add(episodes[i])
            }
            episodesRecycler.adapter = EpisodeAdapter(
                reversedEpisodes,
                "ReooPAxPMsHM4KPMY",
                supportFragmentManager
            )
        }
    }
}