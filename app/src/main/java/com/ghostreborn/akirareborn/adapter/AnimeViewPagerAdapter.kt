package com.ghostreborn.akirareborn.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.fragment.AnilistFragment
import com.ghostreborn.akirareborn.fragment.AnilistLoginFragment
import com.ghostreborn.akirareborn.fragment.AnimeFragment
import com.ghostreborn.akirareborn.fragment.MangaFragment
import com.ghostreborn.akirareborn.fragment.SettingsFragment

class AnimeViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                if (!Constants.preferences.getBoolean(Constants.PREF_LOGGED_IN, false)) {
                    AnilistLoginFragment()
                } else {
                    AnilistFragment()
                }
            }

            1 -> AnimeFragment()
            2 -> MangaFragment()
            3 -> SettingsFragment()

            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}