package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AnilistFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_anilist, container, false).apply {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navView: BottomNavigationView = view.findViewById(R.id.anilist_bottom_navigation)
        childFragmentManager.beginTransaction()
            .replace(R.id.anilist_frame_layout, AnilistAnimeFragment())
            .commit()
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_anime -> {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.anilist_frame_layout, AnilistAnimeFragment())
                        .commit()
                    true
                }

                R.id.nav_manga -> {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.anilist_frame_layout, AnilistMangaFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }

    }
}