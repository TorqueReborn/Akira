package com.ghostreborn.akira.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.anilist.AnilistParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeFragment(private val num: Int) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = LayoutInflater.from(context).inflate(R.layout.common_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.common_recycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        val emptyCard: CardView = view.findViewById(R.id.empty_card)
        CoroutineScope(Dispatchers.IO).launch {
            val anime = AnilistParser().trending(num)
            withContext(Dispatchers.Main) {
                if (anime.isNotEmpty()) {
                    recycler.adapter = AnimeAdapter(anime)
                } else {
                    emptyCard.visibility = View.VISIBLE
                }
            }
        }
    }
}