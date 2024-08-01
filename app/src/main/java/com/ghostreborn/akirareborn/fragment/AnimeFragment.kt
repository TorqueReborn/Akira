package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.adapter.AnimeAdapter
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anime, container, false)
        recyclerView = view.findViewById(R.id.anime_recycler_view)
        searchView = view.findViewById(R.id.anime_search_view)
        progressBar = view.findViewById(R.id.anime_progress_bar)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCoroutine("")
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                setCoroutine(searchView.query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    fun setCoroutine(anime: String){
        progressBar.visibility = ProgressBar.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val animes = AllAnimeParser().searchAnime(anime)
            withContext(Dispatchers.Main) {
                progressBar.visibility = ProgressBar.GONE
                recyclerView.adapter = AnimeAdapter(animes)
                recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            }
        }
    }

    companion object{
        var allAnimeID: String = ""
        var animeThumbnail: String = ""
        var animeEpisode: String = ""
        var animeUrl: String = ""
    }
}