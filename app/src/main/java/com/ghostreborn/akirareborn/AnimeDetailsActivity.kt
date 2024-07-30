package com.ghostreborn.akirareborn

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akirareborn.allanime.AllAnimeParser
import com.ghostreborn.akirareborn.databinding.ActivityAnimeDetailsBinding
import com.ghostreborn.akirareborn.ui.EpisodeActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeDetailsActivity : AppCompatActivity() {

    private lateinit var animeDetailsBinding: ActivityAnimeDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animeDetailsBinding = ActivityAnimeDetailsBinding.inflate(layoutInflater)
        setContentView(animeDetailsBinding.root)

        CoroutineScope(Dispatchers.IO).launch {
            AllAnimeParser().animeDetails(Constants.anime.id)
            withContext(Dispatchers.Main) {
                animeDetailsBinding.animeName.text = Constants.animeDetails.name
                animeDetailsBinding.animeDescription.text = Constants.animeDetails.description
                Picasso.get().load(Constants.animeDetails.banner)
                    .into(animeDetailsBinding.animeBanner)
                Picasso.get().load(Constants.animeDetails.thumbnail)
                    .into(animeDetailsBinding.animeThumbnail)
                if (Constants.animeDetails.prequel.isNotEmpty()) {
                    animeDetailsBinding.prequelButton.visibility = View.VISIBLE
                    animeDetailsBinding.prequelButton.setOnClickListener {
                        Constants.anime.id = Constants.animeDetails.prequel
                        startActivity(Intent(baseContext, AnimeDetailsActivity::class.java))
                        finish()
                    }
                }
                if (Constants.animeDetails.sequel.isNotEmpty()) {
                    animeDetailsBinding.sequelButton.visibility = View.VISIBLE
                    Constants.anime.id = Constants.animeDetails.sequel
                    startActivity(Intent(baseContext, AnimeDetailsActivity::class.java))
                    finish()
                }
                animeDetailsBinding.watchFab.setOnClickListener {
                    startActivity(Intent(baseContext, EpisodeActivity::class.java))
                }
            }
        }
    }
}