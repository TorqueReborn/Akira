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
import com.ghostreborn.akira.adapter.MangaAdapter
import com.ghostreborn.akira.allManga.AllMangaParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MangaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manga, container, false)
        recyclerView = view.findViewById(R.id.manga_recycler_view)
        searchView = view.findViewById(R.id.manga_search_view)
        swipeRefreshLayout = view.findViewById(R.id.manga_swipe_refresh)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout.setOnRefreshListener { fetchManga("") }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                fetchManga(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = true
        })
        fetchManga("")
    }

    private fun fetchManga(manga: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val mangas = AllMangaParser().searchManga(manga)
            withContext(Dispatchers.Main) {
                recyclerView.adapter = MangaAdapter(mangas)
                recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

}