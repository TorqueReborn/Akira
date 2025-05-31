package com.ghostreborn.akira.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.SeasonalAdapter
import com.ghostreborn.akira.allAnime.AnimeSeason
import com.ghostreborn.akira.model.AnimeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeasonalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seasonal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        page = 1

        val seasonalRecycler = findViewById<RecyclerView>(R.id.seasonal_recycler)

        val intent = intent
        if (intent != null) {
            val season = intent.getStringExtra("animeSeason")!!.split(" ")
            findViewById<TextView>(R.id.seasonal_text).text = intent.getStringExtra("animeSeason")
            CoroutineScope(Dispatchers.IO).launch {
                val anime = AnimeSeason().animeBySeasonYear(season[0], season[1], page)
                withContext(Dispatchers.Main) {
                    val adapter = SeasonalAdapter(mutableListOf(anime) as ArrayList<AnimeItem>)
                    seasonalRecycler.adapter = adapter
                    seasonalRecycler.layoutManager = LinearLayoutManager(applicationContext)
                }
            }
        }

    }

    companion object {
        var page: Int = 1
    }
}