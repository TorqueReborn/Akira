package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.Utils
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.adapter.SearchAdapter
import com.ghostreborn.akira.allAnime.AnimeSearch
import com.ghostreborn.akira.allAnime.AnimeSeason
import com.ghostreborn.akira.model.Anime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeasonalFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_seasonal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.seasonal_recycler)
        var adapter: AnimeAdapter
        val searchView = view.findViewById<SearchView>(R.id.seasonal_search)
        searchView.setIconifiedByDefault(false)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                CoroutineScope(Dispatchers.IO).launch {
                    page = 1
                    val anime = AnimeSearch().animeSearch(query.toString())

                    withContext(Dispatchers.Main) {
                        val adapter = SearchAdapter(query.toString(), mutableListOf(anime) as ArrayList<Anime>)
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(context)
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
                recyclerView.layoutManager = LinearLayoutManager(context)
            }
        }
    }

    companion object {
        var count = 0
        var page = 1
    }

}