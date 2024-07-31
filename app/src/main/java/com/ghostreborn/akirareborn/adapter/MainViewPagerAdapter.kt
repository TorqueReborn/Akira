package com.ghostreborn.akirareborn.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ghostreborn.akirareborn.fragment.AnilistFragment
import com.ghostreborn.akirareborn.fragment.HomeFragment
import com.ghostreborn.akirareborn.test.TestFragment

class MainViewPagerAdapter (fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 3 // Number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AnilistFragment()
            1 -> HomeFragment()
            2 -> TestFragment()
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}