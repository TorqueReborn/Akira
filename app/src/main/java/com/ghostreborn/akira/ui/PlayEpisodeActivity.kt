package com.ghostreborn.akira.ui

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.Constants.allAnimeID
import com.ghostreborn.akira.R
import com.ghostreborn.akira.allAnime.AllAnimeParser
import com.ghostreborn.akira.anilist.AnilistParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PlayEpisodeActivity : AppCompatActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private val videoUrl: Uri by lazy { Uri.parse(Constants.animeUrl) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_episode)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val playerView = findViewById<PlayerView>(R.id.anime_video_view)
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer

        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        playerView.setOnClickListener {
            hideSystemBars()
        }

        hideSystemBars()
        monitorVideoProgress()

    }

    override fun onStop() {
        super.onStop()
        exoPlayer.release()
    }

    private fun hideSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.displayCutout())
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    private fun monitorVideoProgress() {
        val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        coroutineScope.launch {
            while (true) {
                delay(1000)
                if (exoPlayer.duration > 0 && exoPlayer.currentPosition.toFloat() / exoPlayer.duration >= 0.75) {
                    val saved = withContext(Dispatchers.IO) {
                        val anilistID = AllAnimeParser().anilistWithAllAnimeID(allAnimeID)
                        AnilistParser().saveAnime(
                            anilistID,
                            "CURRENT",
                            Constants.animeEpisode,
                            baseContext,
                            false,
                            this@PlayEpisodeActivity
                        )
                    }
                    if (saved) {
                        Toast.makeText(baseContext, "Saved!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(baseContext, "Failed to save!", Toast.LENGTH_SHORT).show()
                    }
                    break
                }
            }
        }
    }

}