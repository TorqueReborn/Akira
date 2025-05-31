package com.ghostreborn.akira.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ghostreborn.akira.MainActivity
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.EpisodeAdapter
import com.ghostreborn.akira.api.allAnime.AnimeServers
import com.ghostreborn.akira.api.allAnime.FullDetails
import com.ghostreborn.akira.database.Akira
import com.ghostreborn.akira.database.AkiraDatabase
import com.ghostreborn.akira.fragment.anime.ServerFragment
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
        val favoritesButton = findViewById<TextView>(R.id.favorites_button)
        val loadingProgress = findViewById<ProgressBar>(R.id.loading_progress)

        val intent = intent
        if (intent != null && MainActivity.internetAvailable) {
            val id = intent.getStringExtra("animeID")
            CoroutineScope(Dispatchers.IO).launch {
                val animeDetail = FullDetails().fullDetail(id.toString())
                if(animeDetail != null) {
                    withContext(Dispatchers.Main) {

                        if (animeDetail.animePrequel.isNotEmpty()) {
                            animePrequel.visibility = View.VISIBLE
                            animePrequel.setOnClickListener {
                                startActivity(
                                    Intent(
                                        this@DetailsActivity,
                                        DetailsActivity::class.java
                                    ).apply {
                                        putExtra("animeID", animeDetail.animePrequel)
                                    })
                            }
                        }

                        if (animeDetail.animeSequel.isNotEmpty()) {
                            animeSequel.visibility = View.VISIBLE
                            animeSequel.setOnClickListener {
                                startActivity(
                                    Intent(
                                        this@DetailsActivity,
                                        DetailsActivity::class.java
                                    ).apply {
                                        putExtra("animeID", animeDetail.animeSequel)
                                    })
                            }
                        }

                        recycler.adapter = EpisodeAdapter(
                            id.toString(),
                            animeDetail.animeEpisodes.take(5),
                            supportFragmentManager
                        )

                        moreButton.setOnClickListener {
                            startActivity(
                                Intent(
                                    this@DetailsActivity,
                                    EpisodesActivity::class.java
                                ).apply {
                                    putStringArrayListExtra(
                                        "animeEpisodes",
                                        animeDetail.animeEpisodes
                                    )
                                    putExtra("animeID", id.toString())
                                })
                        }

                        watchButton.setOnClickListener {
                            CoroutineScope(Dispatchers.IO).launch {
                                val servers = AnimeServers().servers(
                                    id.toString(),
                                    animeDetail.animeEpisodes[0]
                                )
                                if(servers != null) {
                                    withContext(Dispatchers.Main) {
                                        ServerFragment(servers).show(supportFragmentManager, "server")
                                    }
                                }
                            }
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = AkiraDatabase.getDatabase(this@DetailsActivity).akiraDao()
                            val akira = dao.get(id.toString())
                            withContext(Dispatchers.Main) {
                                withContext(Dispatchers.Main) {
                                    if(akira != null) {
                                        favoritesButton.text = getString(R.string.remove_from_favorites)
                                    } else {
                                        favoritesButton.text = getString(R.string.add_to_favorites)
                                    }
                                }
                            }
                        }

                        favoritesButton.setOnClickListener {
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = AkiraDatabase.getDatabase(this@DetailsActivity).akiraDao()
                                val akira = dao.get(id.toString())
                                if(akira == null) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        dao.insert(Akira(id.toString()))
                                        withContext(Dispatchers.Main) {
                                            favoritesButton.text = getString(R.string.remove_from_favorites)
                                            Toast.makeText(this@DetailsActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        dao.delete(id.toString())
                                        withContext(Dispatchers.Main) {
                                            favoritesButton.text = getString(R.string.add_to_favorites)
                                            Toast.makeText(this@DetailsActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
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