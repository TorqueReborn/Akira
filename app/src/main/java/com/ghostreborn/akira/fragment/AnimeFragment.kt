package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.allAnime.AllAnimeParser
import com.ghostreborn.akira.utils.AkiraUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cardView: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_anime, container, false).apply {
        recyclerView = findViewById(R.id.anime_recycler_view)
        cardView = findViewById(R.id.anime_card)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchAnime()
    }

    private fun fetchAnime() {
        CoroutineScope(Dispatchers.IO).launch {
            AkiraUtils().getLatestPackage()
            val animes = AllAnimeParser().searchAnime("")
            withContext(Dispatchers.Main) {
                cardView.visibility = View.VISIBLE
                recyclerView.adapter = AnimeAdapter(animes)
                recyclerView.layoutManager =
                    GridLayoutManager(context, 1, RecyclerView.HORIZONTAL, false)
            }
        }
    }
}