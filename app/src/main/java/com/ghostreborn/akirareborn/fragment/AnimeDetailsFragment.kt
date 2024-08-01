package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.database.AnilistDatabase
import com.ghostreborn.akirareborn.databinding.FragmentAnimeDetailsBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAnimeDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnimeDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAnilist()

        CoroutineScope(Dispatchers.IO).launch {
            val details = AllAnimeParser().animeDetails(AnimeFragment.allAnimeID)
            withContext(Dispatchers.Main) {
                binding.animeName.text = details.name
                binding.animeDescription.text = details.description
                Picasso.get().load(details.banner).into(binding.animeBanner)
                Picasso.get().load(details.thumbnail).into(binding.animeThumbnail)
            }
        }

    }

    private fun checkAnilist() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(
                requireContext(),
                AnilistDatabase::class.java,
                "Akira"
            ).build()
            val anilist = db.anilistDao().findByAllAnimeID(AnimeFragment.allAnimeID)
            withContext(Dispatchers.Main) {
                if (anilist != null) {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.anilist_frame_layout, SaveAnimeFragment())
                        .commit()
                }
            }
        }
    }
}