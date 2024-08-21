package com.ghostreborn.akira

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.adapter.MangaAdapter
import com.ghostreborn.akira.allAnime.AllAnimeParser
import com.ghostreborn.akira.allManga.AllMangaParser
import com.ghostreborn.akira.anilist.AnilistUtils
import com.ghostreborn.akira.model.Anime
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
                setupAnime(animes)
                setupManga(mangas)
            }
        }

    }

    private fun setupAnime(anime: ArrayList<Anime>){
        findViewById<LinearLayout>(R.id.anime_linear_layout).visibility = if (anime.isEmpty()) View.GONE else View.VISIBLE
        findViewById<RecyclerView>(R.id.home_anime_recycler).apply {
            adapter = AnimeAdapter(anime)
            layoutManager = GridLayoutManager(context, 1, RecyclerView.HORIZONTAL, false)
        }
    }

    private fun setupManga(manga: ArrayList<Anime>) {
        findViewById<LinearLayout>(R.id.manga_linear_layout).visibility = if (manga.isEmpty()) View.GONE else View.VISIBLE
        findViewById<RecyclerView>(R.id.home_manga_recycler).apply {
            adapter = MangaAdapter(manga)
            layoutManager = GridLayoutManager(context, 1, RecyclerView.HORIZONTAL, false)
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