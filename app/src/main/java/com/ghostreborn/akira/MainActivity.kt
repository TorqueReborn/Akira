package com.ghostreborn.akira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.adapter.MangaAdapter
import com.ghostreborn.akira.allAnime.AllAnimeParser
import com.ghostreborn.akira.allManga.AllMangaParser
import com.ghostreborn.akira.anilist.AnilistUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleIntentData()
        CoroutineScope(Dispatchers.IO).launch {
            val animes = AllAnimeParser().searchAnime("")
            val mangas = AllMangaParser().searchManga("")
            withContext(Dispatchers.Main) {
                val recyclerView:RecyclerView = findViewById(R.id.home_anime_recycler)
                val mangaRecyclerView:RecyclerView = findViewById(R.id.home_manga_recycler)
                recyclerView.adapter = AnimeAdapter(animes)
                mangaRecyclerView.adapter = MangaAdapter(mangas)
                mangaRecyclerView.layoutManager = GridLayoutManager(baseContext, 1, RecyclerView.HORIZONTAL, false)
                recyclerView.layoutManager = GridLayoutManager(baseContext, 1, RecyclerView.HORIZONTAL, false)
            }
        }

    }

    private fun handleIntentData() {
        intent.data?.getQueryParameter("code")?.let { code ->
            CoroutineScope(Dispatchers.IO).launch {
                AnilistUtils().getToken(code, this@MainActivity)
            }
        }
    }
}