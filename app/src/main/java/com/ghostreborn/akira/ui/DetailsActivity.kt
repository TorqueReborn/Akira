package com.ghostreborn.akira.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.api.AnilistAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        CoroutineScope(Dispatchers.IO).launch {
            val api = AnilistAPI()
            val details = api.details(Constants.animeID)
            withContext(Dispatchers.Main){
                findViewById<TextView>(R.id.anime_name).text = details.title
                val watched = "${details.lastEpisode} Episodes Available"
                findViewById<TextView>(R.id.anime_episodes).text = watched
                findViewById<TextView>(R.id.anime_studio).text = details.studio
                findViewById<TextView>(R.id.anime_desc).text = details.description
                findViewById<TextView>(R.id.countdown_time).text = details.timeLeft
                findViewById<ImageView>(R.id.anime_thumbnail).load(details.thumbnail)
            }
        }

    }
}