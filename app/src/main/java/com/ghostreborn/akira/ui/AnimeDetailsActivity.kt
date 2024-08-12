package com.ghostreborn.akira.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.ghostreborn.akira.R
import com.ghostreborn.akira.fragment.DetailsFragment

class AnimeDetailsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anime_details)

        supportFragmentManager.beginTransaction()
            .replace(R.id.details_frame_layout, DetailsFragment())
            .commit()

    }

}