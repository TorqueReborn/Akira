package com.ghostreborn.akirareborn.ui

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.anilist.AnilistParser
import com.ghostreborn.akirareborn.fragment.AnimeFragment
import com.ghostreborn.akirareborn.fragment.AnimeFragment.Companion.allAnimeID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PlayEpisodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_episode)

        hideSystemBars()

        val videoView = findViewById<VideoView>(R.id.anime_video_view)
        videoView.setVideoURI(Uri.parse(AnimeFragment.animeUrl))
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setOnClickListener {
            hideSystemBars()
        }
        videoView.setMediaController(mediaController)
        videoView.start()

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(100)
                val progress = videoView.currentPosition.toFloat() / videoView.duration.toFloat()
                if (videoView.duration > 0 && progress >= 0.75) {
                    val anilistID = AllAnimeParser().anilistWithAllAnimeID(allAnimeID)
                    AnilistParser().saveAnime(
                        anilistID,
                        "CURRENT",
                        AnimeFragment.animeEpisode,
                        baseContext
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(baseContext, "Saved!", Toast.LENGTH_SHORT).show()
                    }
                    break
                }
            }
        }

    }

    private fun hideSystemBars() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }
}