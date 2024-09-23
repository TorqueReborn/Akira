package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.api.KitsuAPI
import com.ghostreborn.akira.models.Anime
import com.ghostreborn.akira.utils.AkiraUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private val anime: ArrayList<Anime> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentRecycler = view.findViewById<RecyclerView>(R.id.anime_recycler)
        val search = view.findViewById<SearchView>(R.id.anime_search)

        val db = AkiraUtils().getDB(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            anime.clear()
            val savedAnime = db.savedEntryDao().getCurrent()
            for (saved in savedAnime) {
                anime.add(
                    Anime(
                        saved.animeID,
                        saved.anime,
                        saved.progress,
                        saved.episodeCount,
                        saved.thumbnail
                    )
                )
            }

            withContext(Dispatchers.Main) {
                currentRecycler.adapter = AnimeAdapter(anime)
                currentRecycler.layoutManager = LinearLayoutManager(requireContext())
            }
        }

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                CoroutineScope(Dispatchers.IO).launch {
                    anime.clear()
                    val animeResponse = KitsuAPI().search(query)
                    if (animeResponse!=null) {
                        for (animeEntry in animeResponse.data) {
                            anime.add(
                                Anime(
                                    animeEntry.id,
                                    animeEntry.attributes.canonicalTitle,
                                    "0",
                                    animeEntry.attributes.episodeCount.toString(),
                                    animeEntry.attributes.posterImage.medium
                                )
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        currentRecycler.adapter = AnimeAdapter(anime)
                        currentRecycler.layoutManager = LinearLayoutManager(requireContext())
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })

    }
}