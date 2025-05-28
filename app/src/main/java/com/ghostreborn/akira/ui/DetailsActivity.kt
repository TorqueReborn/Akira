package com.ghostreborn.akira.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.EpisodeAdapter
import com.ghostreborn.akira.allAnime.FullDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recycler = findViewById<RecyclerView>(R.id.episodeRecycler)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val animeName = findViewById<TextView>(R.id.animeName)
        val animeDescription = findViewById<TextView>(R.id.animeDescription)
        val animeBanner = findViewById<ImageView>(R.id.animeBanner)
        val animeImage = findViewById<ImageView>(R.id.animeImage)
        val animeSequel = findViewById<TextView>(R.id.animeSequel)
        val animePrequel = findViewById<TextView>(R.id.animePrequel)
        val moreButton = findViewById<TextView>(R.id.more_button)
        val watchButton = findViewById<TextView>(R.id.watch_button)
        val loadingProgress = findViewById<ProgressBar>(R.id.loading_progress)

        val intent = intent
        if(intent != null) {
            val id = intent.getStringExtra("animeID")
            CoroutineScope(Dispatchers.IO).launch {
                val animeDetail = FullDetails().fullDetail(id.toString())
                withContext(Dispatchers.Main) {
                    if(animeDetail == null) {
                        finish()
                    }

                    if(animeDetail != null) {
                        if(animeDetail.animePrequel.isNotEmpty()) {
                            animePrequel.visibility = View.VISIBLE
                            animePrequel.setOnClickListener {
                                startActivity(Intent(this@DetailsActivity, DetailsActivity::class.java).apply {
                                    putExtra("animeID", animeDetail.animePrequel)
                                })
                            }
                        }

                        if(animeDetail.animeSequel.isNotEmpty()) {
                            animeSequel.visibility = View.VISIBLE
                            animeSequel.setOnClickListener {
                                startActivity(Intent(this@DetailsActivity, DetailsActivity::class.java).apply {
                                    putExtra("animeID", animeDetail.animeSequel)
                                })
                            }
                        }

                        recycler.adapter = EpisodeAdapter(animeDetail.animeEpisodes.take(5), id.toString(), supportFragmentManager)

                        moreButton.setOnClickListener {
                            startActivity(Intent(this@DetailsActivity, EpisodesActivity::class.java).apply {
                                putStringArrayListExtra("animeEpisodes", animeDetail.animeEpisodes)
                            })
                        }

                        animeName.text = animeDetail.animeName
                        animeDescription.text = animeDetail.animeDescription
                        animeImage.load(animeDetail.animeThumbnail)
                        animeBanner.load(animeDetail.animeBanner)
                        loadingProgress.visibility = View.GONE
                    }
                }
            }
        }

    }
}