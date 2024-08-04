package com.ghostreborn.akira.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.fragment.AnilistFragment
import com.ghostreborn.akira.fragment.AnilistLoginFragment
import com.ghostreborn.akira.fragment.MangaFragment
import com.ghostreborn.akira.fragment.SettingsFragment
import com.ghostreborn.akira.fragment.TestFragment

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

            1 -> TestFragment()
            2 -> MangaFragment()
            3 -> SettingsFragment()
            else -> throw IllegalStateException("Invalid tab position")
        }
    }
}