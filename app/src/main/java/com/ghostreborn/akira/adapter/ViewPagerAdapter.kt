package com.ghostreborn.akira.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ghostreborn.akira.fragment.anime.FavoritesFragment
import com.ghostreborn.akira.fragment.anime.SeasonalFragment

class ViewPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
    private val NUM_PAGES = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SeasonalFragment()
            1 -> FavoritesFragment()
            else -> SeasonalFragment()
        }
    }

    override fun getItemCount(): Int = NUM_PAGES
}