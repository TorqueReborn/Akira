package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.MangaAdapter
import com.ghostreborn.akira.allManga.AllMangaParser
import com.ghostreborn.akira.database.AnilistDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnilistMangaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anilist_manga, container, false)
        recyclerView = view.findViewById(R.id.anilist_recycler_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchData()
    }

    private fun fetchData() {

        CoroutineScope(Dispatchers.IO).launch {
            val instance = Room.databaseBuilder(
                requireContext(), AnilistDatabase::class.java, "Akira"
            ).build()

            val ids = instance.anilistDao().getAllManga()
                .joinToString(separator = ",") { "\"${it.allAnimeID}\"" }

            val animes = AllMangaParser().getDetailsByIds(ids)

            withContext(Dispatchers.Main) {
                recyclerView.apply {
                    adapter = MangaAdapter(animes)
                    layoutManager = GridLayoutManager(requireContext(), 3)
                }
            }
        }
    }

}