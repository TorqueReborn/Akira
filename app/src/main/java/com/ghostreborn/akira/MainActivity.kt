package com.ghostreborn.akira

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.adapter.MangaAdapter
import com.ghostreborn.akira.ui.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val animeRecycler = findViewById<RecyclerView>(R.id.anime_recycler)
        animeRecycler.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        val mangaRecycler = findViewById<RecyclerView>(R.id.manga_recycler)
        mangaRecycler.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        CoroutineScope(Dispatchers.IO).launch {
            val anime = Constants.api.recent()
            val manga = Constants.mangaApi.recent()
            withContext(Dispatchers.Main) {
                animeRecycler.adapter = AnimeAdapter(anime)
                mangaRecycler.adapter = MangaAdapter(manga)
            }
        }

        findViewById<CardView>(R.id.anime_more_card).setOnClickListener {
            Constants.isManga = false
            startActivity(Intent(this, HomeActivity::class.java))
        }
        findViewById<CardView>(R.id.manga_more_card).setOnClickListener {
            Constants.isManga = true
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

}