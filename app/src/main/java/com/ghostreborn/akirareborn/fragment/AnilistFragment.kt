package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.adapter.AnimeAdapter
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.database.AnilistDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnilistFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anilist, container, false)
        recyclerView = view.findViewById(R.id.anilist_recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.anilist_swipe_refresh)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout.setOnRefreshListener {
            fetchData()
        }
        fetchData()
    }

    fun fetchData(){
        CoroutineScope(Dispatchers.IO).launch {
            val instance = Room.databaseBuilder(
                requireContext(),
                AnilistDatabase::class.java,
                "Akira"
            ).build()
            val anilists = instance.anilistDao().getAll()
            var ids = ""
            for (i in 0 until anilists.size) {
                ids += "\"" + anilists[i].allAnimeID + "\","
            }
            ids = ids.substring(0, ids.length - 1)
            val animes = AllAnimeParser().getDetailsByIds(ids)
            withContext(Dispatchers.Main) {
                recyclerView.adapter = AnimeAdapter(animes)
                recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}