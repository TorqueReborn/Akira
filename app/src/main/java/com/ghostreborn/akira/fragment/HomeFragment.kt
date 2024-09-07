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

    val currentAnime: ArrayList<Anime> = ArrayList()
    val completedAnime: ArrayList<Anime> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentRecycler = view.findViewById<RecyclerView>(R.id.current_recycler)
        val completedRecycler = view.findViewById<RecyclerView>(R.id.completed_recycler)

        val db = AkiraUtils().getDB(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            val list = db.savedEntryDao().getCurrent()
            for (entry in list) {
                currentAnime.add(Anime(entry.kitsuID, entry.anime, entry.progress,entry.thumbnail))
            }

            withContext(Dispatchers.Main) {
                val adapter = AnimeAdapter(currentAnime)
                currentRecycler.adapter = adapter
                currentRecycler.layoutManager = LinearLayoutManager(requireContext())
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val list = db.savedEntryDao().getCompleted()
            for (entry in list) {
                completedAnime.add(Anime(entry.kitsuID, entry.anime, entry.progress,entry.thumbnail))
            }

            withContext(Dispatchers.Main) {
                val adapter = AnimeAdapter(completedAnime)
                completedRecycler.adapter = adapter
                completedRecycler.layoutManager = LinearLayoutManager(requireContext())
            }
        }


    }
}