package com.ghostreborn.akira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.adapter.MangaAdapter
import com.ghostreborn.akira.model.Manga
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler = findViewById<RecyclerView>(R.id.manga_recycler_view).apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
        }

        val searchView = findViewById<SearchView>(R.id.manga_search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also {
                query?.let { searchAnime(it, recycler) }
            }

            override fun onQueryTextChange(newText: String?) = true
        })

        recentManga(recycler)
    }

    private fun recentManga(recycler: RecyclerView) {
        ioScope.launch {
            val manga = Constants.mangaApi.recent()
            updateRecycler(recycler, manga)
        }
    }

    private fun searchAnime(query: String, recycler: RecyclerView) {
        ioScope.launch {
            val manga = Constants.mangaApi.search(query)
            updateRecycler(recycler, manga)
        }
    }

    private suspend fun updateRecycler(recycler: RecyclerView, manga: ArrayList<Manga>) {
        withContext(Dispatchers.Main) {
            recycler.adapter = MangaAdapter(manga)
        }
    }
}