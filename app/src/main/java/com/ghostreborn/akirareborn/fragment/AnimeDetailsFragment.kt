package com.ghostreborn.akirareborn.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ghostreborn.akirareborn.ui.EpisodesActivity
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
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

        childFragmentManager.beginTransaction()
            .replace(R.id.anilist_frame_layout, SaveAnimeFragment())
            .commit()

        CoroutineScope(Dispatchers.IO).launch {
            val details = AllAnimeParser().animeDetails(AnimeFragment.allAnimeID)
            withContext(Dispatchers.Main) {
                binding.animeName.text = details.name
                binding.animeDescription.text = details.description
                Picasso.get().load(details.banner).into(binding.animeBanner)
                Picasso.get().load(details.thumbnail).into(binding.animeThumbnail)
                binding.watchFab.setOnClickListener {
                    startActivity(Intent(requireContext(), EpisodesActivity::class.java))
                }
            }
        }

    }
}