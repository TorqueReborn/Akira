package com.ghostreborn.akirareborn.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.fragment.AnimeDetailsFragment

class AnimeDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anime_details)
        supportFragmentManager.beginTransaction()
            .replace(R.id.anime_details_frame_layout, AnimeDetailsFragment())
            .commit()
    }
}