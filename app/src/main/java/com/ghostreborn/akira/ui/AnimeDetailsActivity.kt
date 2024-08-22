package com.ghostreborn.akira.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.allAnime.AllAnimeParser
import com.ghostreborn.akira.databinding.ActivityAnimeDetailsBinding
import com.ghostreborn.akira.fragment.SaveAnimeFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnimeDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.anilist_frame_layout, SaveAnimeFragment())
            .commit()

        CoroutineScope(Dispatchers.IO).launch {
            val details = AllAnimeParser().animeDetails(Constants.allAnimeID)
            withContext(Dispatchers.Main) {
                binding.apply {
                    animeName.text = details.name
                    animeDescription.text = details.description
                    animeBanner.load(details.banner)
                    animeThumbnail.load(details.thumbnail)

                    binding.animeProgressTextView.text = if (Constants.animeEpisode.isNotEmpty()) {
                        "Watched ${Constants.animeEpisode} episodes"
                    } else {
                        "Not watched"
                    }

                    watchFab.setOnClickListener {
                        startActivity(Intent(this@AnimeDetailsActivity, EpisodesActivity::class.java))
                    }

                    prequelButton.apply {
                        visibility = if (details.prequel.isNotEmpty()) View.VISIBLE else View.GONE
                        setOnClickListener {
                            Constants.allAnimeID = details.prequel
                            Constants.animeEpisode = ""
                            startActivity(Intent(context, AnimeDetailsActivity::class.java))
                        }
                    }

                    sequelButton.apply {
                        visibility = if (details.sequel.isNotEmpty()) View.VISIBLE else View.GONE
                        setOnClickListener {
                            Constants.allAnimeID = details.sequel
                            Constants.animeEpisode = ""
                            startActivity(Intent(context, AnimeDetailsActivity::class.java))
                        }
                    }
                }
            }
        }

    }

}