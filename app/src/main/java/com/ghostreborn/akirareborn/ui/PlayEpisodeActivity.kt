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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_episode)
        hideSystemBars()

        videoView = findViewById(R.id.anime_video_view)
        videoView.setVideoURI(videoUrl)

        val mediaController = MediaController(this).apply {
            setAnchorView(videoView)
        }
        videoView.setMediaController(mediaController)
        videoView.setOnClickListener { hideSystemBars() }
        videoView.start()

        monitorVideoProgress()
    }

    private fun hideSystemBars() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }

    private fun monitorVideoProgress() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(100)
                if (videoView.duration > 0 && videoView.currentPosition.toFloat() / videoView.duration >= 0.75) {
                    val anilistID = AllAnimeParser().anilistWithAllAnimeID(allAnimeID)
                    val saved = AnilistParser().saveAnime(
                        anilistID,
                        "CURRENT",
                        Constants.animeEpisode,
                        baseContext
                    )
                    withContext(Dispatchers.Main) {
                        if (saved){
                            Toast.makeText(baseContext, "Saved!", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(baseContext, "Failed to save!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    break
                }
            }
        }
    }
}