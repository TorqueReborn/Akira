package com.ghostreborn.akira

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.adapter.SearchAdapter
import com.ghostreborn.akira.allAnime.AnimeSearch
import com.ghostreborn.akira.allAnime.AnimeSeason
import com.ghostreborn.akira.model.Anime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.main_recycler)
        var adapter: AnimeAdapter
        val searchView = findViewById<SearchView>(R.id.main_search)
        searchView.setIconifiedByDefault(false)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                CoroutineScope(Dispatchers.IO).launch {
                    page = 1
                    val anime = AnimeSearch().animeSearch(query.toString())

                    withContext(Dispatchers.Main) {
                        val adapter = SearchAdapter(query.toString(), mutableListOf(anime) as ArrayList<Anime>)
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        CoroutineScope(Dispatchers.IO).launch {
            val season = Utils().calculateQuarter(0)
            val anime = AnimeSeason().animeBySeasonYear(season.first, season.second)
            count++

            withContext(Dispatchers.Main) {
                adapter = AnimeAdapter(mutableListOf(anime) as ArrayList<Anime>)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(applicationContext)
            }
        }
    }

    companion object {
        var count = 0
        var page = 1
    }
}