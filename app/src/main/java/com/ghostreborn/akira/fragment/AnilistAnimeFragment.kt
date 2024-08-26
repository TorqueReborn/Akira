package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.parsers.anilist.AnilistNetwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnilistAnimeFragment:Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_common,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val anime = AnilistNetwork().getAnimeList(requireActivity())
            withContext(Dispatchers.Main) {
                recycler.adapter = AnimeAdapter(anime)
            }
        }
    }
}