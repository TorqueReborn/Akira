package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private lateinit var animeRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anime, container, false)
        animeRecyclerView = view.findViewById(R.id.anime_recycler_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            val anime = AllAnimeParser().searchAnime("")
            withContext(Dispatchers.Main) {
                animeRecyclerView.adapter = AnimeAdapter(anime)
                animeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            }
        }
    }

    companion object{
        var allAnimeID: String = ""
    }
}