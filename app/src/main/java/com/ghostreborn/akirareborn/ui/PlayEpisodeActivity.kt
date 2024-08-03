package com.ghostreborn.akirareborn.ui

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.Constants.allAnimeID
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.anilist.AnilistParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PlayEpisodeActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private val videoUrl: Uri by lazy { Uri.parse(Constants.animeUrl) }
    var monitorProgress = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_episode)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        videoView = findViewById(R.id.anime_video_view)
        videoView.setVideoURI(videoUrl)

        val mediaController = MediaController(this).apply {
            setAnchorView(videoView)
        }

        videoView.setMediaController(mediaController)
        videoView.setOnClickListener { hideSystemBars() }
        videoView.start()

        findViewById<ConstraintLayout>(R.id.main).setOnClickListener {
            showSystemBars()
        }

        monitorVideoProgress()
    }

    private fun showSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            show(WindowInsetsCompat.Type.statusBars())
            show(WindowInsetsCompat.Type.displayCutout())
            show(WindowInsetsCompat.Type.navigationBars())
            CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                withContext(Dispatchers.Main) {
                    hideSystemBars()
                }
            }
        }
    }

    private fun hideSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.displayCutout())
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    private fun monitorVideoProgress() {
        CoroutineScope(Dispatchers.IO).launch {
            while (monitorProgress) {
                delay(1000)
                if (videoView.duration > 0 && videoView.currentPosition.toFloat() / videoView.duration >= 0.75) {
                    val anilistID = AllAnimeParser().anilistWithAllAnimeID(allAnimeID)
                    val saved = AnilistParser().saveAnime(
                        anilistID,
                        "CURRENT",
                        Constants.animeEpisode,
                        baseContext
                    )
                    withContext(Dispatchers.Main) {
                        if (saved) {
                            Toast.makeText(baseContext, "Saved!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(baseContext, "Failed to save!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    break
                }
            }
        }
    }
}