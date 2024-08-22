package com.ghostreborn.akira.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.allManga.AllMangaParser
import com.ghostreborn.akira.databinding.ActivityMangaDetailsBinding
import com.ghostreborn.akira.fragment.SaveMangaFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MangaDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMangaDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMangaDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.anilist_frame_layout, SaveMangaFragment())
            .commit()

        CoroutineScope(Dispatchers.IO).launch {
            val details = AllMangaParser().mangaDetails(Constants.allMangaID)
            withContext(Dispatchers.Main) {
                binding.apply {
                    mangaName.text = details.name
                    mangaDescription.text = details.description
                    mangaBanner.load(details.banner)
                    mangaThumbnail.load(details.thumbnail)

                    binding.mangaProgressTextView.text = if (Constants.mangaChapter.isNotEmpty()) {
                        "Read ${Constants.mangaChapter} chapters"
                    } else {
                        "Not Read"
                    }

                    readFab.setOnClickListener {
                        startActivity(
                            Intent(
                                this@MangaDetailsActivity,
                                ChaptersActivity::class.java
                            )
                        )
                    }

                    mangaPrequelButton.apply {
                        visibility = if (details.prequel.isNotEmpty()) View.VISIBLE else View.GONE
                        setOnClickListener {
                            Constants.allMangaID = details.prequel
                            Constants.mangaChapter = ""
                            startActivity(Intent(context, MangaDetailsActivity::class.java))
                        }
                    }

                    mangaSequelButton.apply {
                        visibility = if (details.sequel.isNotEmpty()) View.VISIBLE else View.GONE
                        setOnClickListener {
                            Constants.allMangaID = details.sequel
                            Constants.mangaChapter = ""
                            startActivity(Intent(context, MangaDetailsActivity::class.java))
                        }
                    }
                }
            }
        }

    }
}