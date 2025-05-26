package com.ghostreborn.akira

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TestFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animeImages = listOf(
            "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx165445-qwVP0w5NIaBW.jpg",
            "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx179979-OiIKSkhJRmvo.jpg",
            "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx178781-pbk5FgfiAJxS.jpg"
        )
        val anime = listOf<Anime>(
            Anime("Spring 2025", animeImages),
            Anime("Spring 2025", animeImages),
            Anime("Spring 2025", animeImages)
        )
        val animeItemAdapter = AnimeItemAdapter(anime)
        val animeRecyclerView = view.findViewById<RecyclerView>(R.id.anime_recycler_view)
        animeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        animeRecyclerView.adapter = animeItemAdapter


    }

}