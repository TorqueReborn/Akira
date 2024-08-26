package com.ghostreborn.akira.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.parsers.anilist.AnilistNetwork
import com.ghostreborn.akira.ui.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnilistAnimeFragment(val enableSearch: Boolean):Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_common, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
        }

        if (!enableSearch){
            recycler.layoutManager = GridLayoutManager(requireContext(), 1, GridLayoutManager.HORIZONTAL, false)
            view.findViewById<CardView>(R.id.common_card).visibility = View.VISIBLE
            view.findViewById<CardView>(R.id.common_more).visibility = View.GONE
            view.findViewById<TextView>(R.id.common_title).text = getString(R.string.saved_anime)
            view.findViewById<CardView>(R.id.common_more).setOnClickListener{
                Constants.isManga = false
                startActivity(Intent(requireActivity(), HomeActivity::class.java))
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val anime = AnilistNetwork().getAnimeList(requireActivity())
            withContext(Dispatchers.Main) {
                recycler.adapter = AnimeAdapter(anime)
            }
        }
    }
}