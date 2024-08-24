package com.ghostreborn.akira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.adapter.AllAnimeAdapter
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.parsers.AllAnime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler = findViewById<RecyclerView>(R.id.anime_recycler_view).apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
        }

        val searchView = findViewById<SearchView>(R.id.anime_search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also {
                query?.let { searchAnime(it, recycler) }
            }

            override fun onQueryTextChange(newText: String?) = true
        })

        fetchRecentAnime(recycler)
    }

    private fun fetchRecentAnime(recycler: RecyclerView) {
        ioScope.launch {
            val anime = AllAnime().recent()
            updateRecycler(recycler, anime)
        }
    }

    private fun searchAnime(query: String, recycler: RecyclerView) {
        ioScope.launch {
            val anime = AllAnime().search(query)
            updateRecycler(recycler, anime)
        }
    }

    private suspend fun updateRecycler(recycler: RecyclerView, anime: ArrayList<Anime>) {
        withContext(Dispatchers.Main) {
            recycler.adapter = AllAnimeAdapter(anime)
        }
    }
}