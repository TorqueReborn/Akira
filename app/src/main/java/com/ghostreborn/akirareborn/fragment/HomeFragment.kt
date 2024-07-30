package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.adapter.AnimeAdapter
import com.ghostreborn.akirareborn.allanime.AllAnimeParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var homeRecycler: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        homeRecycler = view.findViewById(R.id.home_anime_recycler)
        searchView = view.findViewById(R.id.anime_search_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            AllAnimeParser().searchAnime("")
            withContext(Dispatchers.Main) {
                homeRecycler.adapter = AnimeAdapter(requireContext())
                homeRecycler.layoutManager = GridLayoutManager(requireContext(), 3)
            }
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                CoroutineScope(Dispatchers.IO).launch {
                    AllAnimeParser().searchAnime(searchView.query.toString())
                    withContext(Dispatchers.Main) {
                        homeRecycler.adapter = AnimeAdapter(requireContext())
                        homeRecycler.layoutManager = GridLayoutManager(requireContext(), 3)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

}