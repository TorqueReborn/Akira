package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
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

        val db = AkiraUtils().getDB(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            val savedAnime = db.savedEntryDao().getCurrent()
            for (saved in savedAnime) {
                anime.add(Anime(saved.kitsuID, saved.anime, saved.progress,saved.thumbnail))
            }

            withContext(Dispatchers.Main) {
                currentRecycler.adapter =  AnimeAdapter(anime)
                currentRecycler.layoutManager = LinearLayoutManager(requireContext())
            }
        }

    }
}