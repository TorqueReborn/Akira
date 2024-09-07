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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recycler)

        CoroutineScope(Dispatchers.IO).launch {
            val db = AkiraUtils().getDB(requireContext())
            val list = db.savedEntryDao().getAll()
            val anime = ArrayList<Anime>()
            for (entry in list) {
                anime.add(Anime(entry.kitsuID, entry.anime, entry.progress,entry.thumbnail))
            }

            withContext(Dispatchers.Main) {
                val adapter = AnimeAdapter(anime)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        }


    }
}