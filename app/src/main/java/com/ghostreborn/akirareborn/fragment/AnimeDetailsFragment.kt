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
        binding = FragmentAnimeDetailsBinding.inflate(inflater, container, false)
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
                binding.apply {
                    animeName.text = details.name
                    animeDescription.text = details.description
                    Picasso.get().load(details.banner).into(animeBanner)
                    Picasso.get().load(details.thumbnail).into(animeThumbnail)

                    binding.animeProgressTextView.text = if (AnimeFragment.animeEpisode.isNotEmpty()) {
                        "Watched ${AnimeFragment.animeEpisode} episodes"
                    } else {
                        "Not watched"
                    }

                    watchFab.setOnClickListener {
                        startActivity(Intent(context, EpisodesActivity::class.java))
                    }

                    prequelButton.apply {
                        visibility = if (details.prequel.isNotEmpty()) View.VISIBLE else View.GONE
                        setOnClickListener {
                            AnimeFragment.allAnimeID = details.prequel
                            AnimeFragment.animeEpisode = ""
                            startActivity(Intent(context, AnimeDetailsActivity::class.java))
                        }
                    }

                    sequelButton.apply {
                        visibility = if (details.sequel.isNotEmpty()) View.VISIBLE else View.GONE
                        setOnClickListener {
                            AnimeFragment.allAnimeID = details.sequel
                            AnimeFragment.animeEpisode = ""
                            startActivity(Intent(context, AnimeDetailsActivity::class.java))
                        }
                    }
                }
            }
        }
    }
}