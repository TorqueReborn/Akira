package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navView: BottomNavigationView = view.findViewById(R.id.home_bottom_navigation)

        childFragmentManager.beginTransaction()
            .replace(R.id.home_frame_layout, AnimeFragment())
            .commit()

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_anime -> {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.home_frame_layout, AnimeFragment())
                        .commit()
                    true
                }
                R.id.nav_manga -> {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.home_frame_layout, MangaFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}