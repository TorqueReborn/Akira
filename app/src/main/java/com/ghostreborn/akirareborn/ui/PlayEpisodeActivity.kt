package com.ghostreborn.akirareborn.ui

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
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.Constants.allAnimeID
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.anilist.AnilistParser
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

        showSystemBars()
        monitorVideoProgress()

    }

    override fun onStop() {
        super.onStop()
        exoPlayer.release()
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
                            baseContext
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