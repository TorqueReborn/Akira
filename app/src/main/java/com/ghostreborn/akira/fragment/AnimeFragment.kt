package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.gojo.GojoParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_anime, container, false).apply {
        recyclerView = findViewById(R.id.anime_recycler_view)
        searchView = findViewById(R.id.anime_search_view)
        swipeRefreshLayout = findViewById(R.id.anime_swipe_refresh)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout.setOnRefreshListener { fetchAnime("") }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                fetchAnime(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = true
        })
        fetchAnime("")
    }

    private fun fetchAnime(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val animes = GojoParser().recentUpdates()
            withContext(Dispatchers.Main) {
                recyclerView.adapter = AnimeAdapter(animes)
                recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}