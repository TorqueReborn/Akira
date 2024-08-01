package com.ghostreborn.akirareborn.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.databinding.FragmentAnimeDetailsBinding
import com.ghostreborn.akirareborn.ui.AnimeDetailsActivity
import com.ghostreborn.akirareborn.ui.EpisodesActivity
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

    override fun onResume() {
        super.onResume()
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

                if (details.prequel.isNotEmpty()) {
                    binding.prequelButton.visibility = View.VISIBLE
                    binding.prequelButton.setOnClickListener {
                        AnimeFragment.allAnimeID = details.prequel
                        startActivity(Intent(requireContext(), AnimeDetailsActivity::class.java))
                    }
                }
                if (details.sequel.isNotEmpty()) {
                    binding.sequelButton.visibility = View.VISIBLE
                    binding.sequelButton.setOnClickListener {
                        AnimeFragment.allAnimeID = details.sequel
                        startActivity(Intent(requireContext(), AnimeDetailsActivity::class.java))
                    }
                }
            }
        }
    }
}