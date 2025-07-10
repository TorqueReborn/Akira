package com.ghostreborn.akira.fragment.anime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.FavoritesAdapter
import com.ghostreborn.akira.api.allAnime.AnimeById
import com.ghostreborn.akira.database.AkiraDatabase
import com.ghostreborn.akira.model.Anime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesFragment : Fragment() {

    private lateinit var recycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        recycler = view.findViewById(R.id.saved_recycler)
        return view
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            val animeList = AkiraDatabase.getDatabase(requireContext()).akiraDao().getAll()
            if(animeList.isNotEmpty()) {
                val anime = AnimeById().animeByID(animeList[0])
                (animeList as ArrayList<String>).removeAt(0)
                if(anime != null) {
                    val adapter = FavoritesAdapter(mutableListOf(anime) as ArrayList<Anime>, animeList)
                    withContext(Dispatchers.Main) {
                        recycler.adapter = adapter
                        recycler.layoutManager = GridLayoutManager(
                            recycler.context, 3, LinearLayoutManager.VERTICAL, false
                        )
                    }
                }
            }
        }
    }

}