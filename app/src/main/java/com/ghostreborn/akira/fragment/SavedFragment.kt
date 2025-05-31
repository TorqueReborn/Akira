package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.api.allAnime.AnimeById
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_saved, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.saved_recycler)

        CoroutineScope(Dispatchers.IO).launch {
            val anime = AnimeById().animeByID("ReooPAxPMsHM4KPMY")
            val adapter = AnimeAdapter.AnimeItemAdapter(mutableListOf(anime))
            withContext(Dispatchers.Main) {
                recycler.adapter = adapter
                recycler.layoutManager = GridLayoutManager(
                    recycler.context,
                    3,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            }
        }

    }

}