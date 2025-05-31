package com.ghostreborn.akira.fragment.anime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.MainActivity
import com.ghostreborn.akira.R
import com.ghostreborn.akira.utils.Utils
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.adapter.SearchAdapter
import com.ghostreborn.akira.api.allAnime.AnimeSearch
import com.ghostreborn.akira.api.allAnime.AnimeSeason
import com.ghostreborn.akira.model.AnimeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeasonalFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_seasonal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.seasonal_recycler)
        searchView = view.findViewById(R.id.seasonal_search)

        searchView.setIconifiedByDefault(false)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                CoroutineScope(Dispatchers.IO).launch {
                    page = 1
                    val anime = AnimeSearch().animeSearch(query.toString())

                    if(anime != null) {
                        withContext(Dispatchers.Main) {
                            val adapter = SearchAdapter(query.toString(), anime)
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = GridLayoutManager(
                                context,
                                3,
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        }
                    }

                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

    }

    override fun onResume() {
        super.onResume()

        if (MainActivity.internetAvailable) {
            CoroutineScope(Dispatchers.IO).launch {
                val season = Utils().calculateQuarter(0)
                val anime = AnimeSeason().animeBySeasonYear(season.first, season.second)
                count = 0

                if(anime != null) {
                    withContext(Dispatchers.Main) {
                        val adapter = AnimeAdapter(mutableListOf(anime) as ArrayList<AnimeItem>)
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(context)
                    }
                }
            }
        }

    }

    companion object {
        var count = 0
        var page = 1
    }

}