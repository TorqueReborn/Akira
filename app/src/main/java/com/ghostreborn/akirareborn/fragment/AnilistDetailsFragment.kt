package com.ghostreborn.akirareborn.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.allanime.AllAnimeParser
import com.ghostreborn.akirareborn.databinding.FragmentAnilistDetailsBinding
import com.ghostreborn.akirareborn.ui.AnimeDetailsActivity
import com.ghostreborn.akirareborn.ui.EpisodeActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AnilistDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAnilistDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnilistDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            Constants.allAnimeID = AllAnimeParser().allAnimeIdWithMalId(Constants.anilistAnime.title, Constants.anilistAnime.malId)
            AllAnimeParser().animeDetails(Constants.allAnimeID)
            withContext(Dispatchers.Main) {
                binding.animeName.text = Constants.animeDetails.name
                binding.animeDescription.text = Constants.animeDetails.description
                Picasso.get().load(Constants.animeDetails.banner)
                    .into(binding.animeBanner)
                Picasso.get().load(Constants.animeDetails.thumbnail)
                    .into(binding.animeThumbnail)
                if (Constants.animeDetails.prequel.isNotEmpty()) {
                    binding.prequelButton.visibility = View.VISIBLE
                    binding.prequelButton.setOnClickListener {
                        Constants.anime.id = Constants.animeDetails.prequel
                        startActivity(Intent(requireContext(), AnimeDetailsActivity::class.java))
                    }
                }
                if (Constants.animeDetails.sequel.isNotEmpty()) {
                    binding.sequelButton.visibility = View.VISIBLE
                    binding.sequelButton.setOnClickListener {
                        Constants.anime.id = Constants.animeDetails.sequel
                        startActivity(Intent(requireContext(), AnimeDetailsActivity::class.java))
                    }
                }
                binding.watchFab.setOnClickListener {
                    startActivity(Intent(requireContext(), EpisodeActivity::class.java))
                }
            }
        }
    }
}