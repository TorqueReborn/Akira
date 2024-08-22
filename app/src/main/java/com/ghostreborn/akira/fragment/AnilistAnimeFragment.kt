package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.allAnime.AllAnimeParser
import com.ghostreborn.akira.database.AnilistDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnilistAnimeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cardView: CardView
    private lateinit var fragmentTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.common_fragment, container, false).apply {
        recyclerView = findViewById(R.id.anime_recycler_view)
        cardView = findViewById(R.id.fragment_title_card)
        fragmentTitle = findViewById(R.id.fragment_title)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchAnilistAnime()
    }

    private fun fetchAnilistAnime() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1500)
            val instance = Room.databaseBuilder(
                requireContext(), AnilistDatabase::class.java, "Akira"
            ).build()

            val ids = instance.anilistDao().getAllAnime()
                .joinToString(separator = ",") { "\"${it.allAnimeID}\"" }

            val animes = AllAnimeParser().getDetailsByIds(ids)

            withContext(Dispatchers.Main) {
                recyclerView.apply {
                    cardView.visibility = View.VISIBLE
                    recyclerView.visibility = View.VISIBLE
                    fragmentTitle.text = getString(R.string.saved_anime)
                    adapter = AnimeAdapter(animes)
                    layoutManager = GridLayoutManager(requireContext(), 3)
                }
            }
        }
    }

}