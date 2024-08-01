package com.ghostreborn.akirareborn.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ghostreborn.akirareborn.fragment.AnilistFragment
import com.ghostreborn.akirareborn.fragment.AnimeFragment
import com.ghostreborn.akirareborn.fragment.TestFragment

class AnimeViewPagerAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 3 // Number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AnilistFragment()
            1 -> AnimeFragment()
            2 -> TestFragment()
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}